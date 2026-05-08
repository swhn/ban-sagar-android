package com.bansagar.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansagar.app.data.model.Slang
import com.bansagar.app.data.preferences.UserPreferencesRepository
import com.bansagar.app.domain.repository.AuthRepository
import com.bansagar.app.domain.repository.SlangRepository
import com.bansagar.app.domain.repository.VoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val slang: Slang? = null,
    val relatedWords: List<Slang> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showNsfw: Boolean = false,
    val userVote: String? = null,
    val isVoting: Boolean = false,
)

@HiltViewModel
class SlangDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SlangRepository,
    private val voteRepository: VoteRepository,
    private val authRepository: AuthRepository,
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    private val slug: String = savedStateHandle["slug"] ?: ""

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadSlang()
        viewModelScope.launch {
            prefs.showNsfw.collect { show ->
                _uiState.value = _uiState.value.copy(showNsfw = show)
            }
        }
    }

    private fun loadSlang() {
        viewModelScope.launch {
            try {
                val slang = repository.getBySlug(slug)
                if (slang != null) {
                    _uiState.value = _uiState.value.copy(slang = slang, isLoading = false)
                    repository.incrementView(slang.id)
                    loadRelated(slang)
                    loadUserVote(slang.id)
                } else {
                    _uiState.value = _uiState.value.copy(error = "Word not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    private fun loadRelated(slang: Slang) {
        viewModelScope.launch {
            try {
                val related = repository.getRelated(slang)
                _uiState.value = _uiState.value.copy(relatedWords = related)
            } catch (_: Exception) { }
        }
    }

    private fun loadUserVote(slangId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUserFlow.first() ?: return@launch
            val vote = try { voteRepository.getUserVote(user.id, slangId) } catch (_: Exception) { null }
            _uiState.value = _uiState.value.copy(userVote = vote)
        }
    }

    fun castVote(voteType: String) {
        val slang = _uiState.value.slang ?: return
        val prevVote = _uiState.value.userVote
        val newVote = if (prevVote == voteType) null else voteType

        val upDelta = when {
            newVote == "up" -> 1
            prevVote == "up" -> -1
            else -> 0
        }
        val downDelta = when {
            newVote == "down" -> 1
            prevVote == "down" -> -1
            else -> 0
        }
        _uiState.value = _uiState.value.copy(
            userVote = newVote,
            slang = slang.copy(
                upvotes = slang.upvotes + upDelta,
                downvotes = slang.downvotes + downDelta,
            ),
            isVoting = true,
        )

        viewModelScope.launch {
            val user = authRepository.currentUserFlow.first()
            if (user == null) {
                _uiState.value = _uiState.value.copy(userVote = prevVote, slang = slang, isVoting = false)
                return@launch
            }
            try {
                voteRepository.castVote(user.id, slang.id, voteType)
                _uiState.value = _uiState.value.copy(isVoting = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(userVote = prevVote, slang = slang, isVoting = false)
            }
        }
    }
}
