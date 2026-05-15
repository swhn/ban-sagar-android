package com.bansagar.app.ui.contribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansagar.app.data.model.Slang
import com.bansagar.app.data.model.SlangSubmission
import com.bansagar.app.domain.repository.AuthRepository
import com.bansagar.app.domain.repository.ContributeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddSlangUiState(
    val word: String = "",
    val pronunciation: String = "",
    val meaning: String = "",
    val meaningBurmese: String = "",
    val examples: List<String> = listOf(""),
    val isNsfw: Boolean = false,
    val duplicates: List<Slang> = emptyList(),
    val isCheckingDuplicates: Boolean = false,
    val dismissedWarning: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AddSlangViewModel @Inject constructor(
    private val contributeRepository: ContributeRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSlangUiState())
    val uiState: StateFlow<AddSlangUiState> = _uiState.asStateFlow()

    private var dupJob: Job? = null

    fun onWordChange(word: String) {
        _uiState.value = _uiState.value.copy(word = word, dismissedWarning = false)
        dupJob?.cancel()
        if (word.length < 2) {
            _uiState.value = _uiState.value.copy(duplicates = emptyList())
            return
        }
        dupJob = viewModelScope.launch {
            delay(400)
            _uiState.value = _uiState.value.copy(isCheckingDuplicates = true)
            val dups = contributeRepository.checkDuplicates(word)
            _uiState.value = _uiState.value.copy(duplicates = dups, isCheckingDuplicates = false)
        }
    }

    fun dismissWarning() {
        _uiState.value = _uiState.value.copy(dismissedWarning = true)
    }

    fun onPronunciationChange(v: String) { _uiState.value = _uiState.value.copy(pronunciation = v) }
    fun onMeaningChange(v: String) { _uiState.value = _uiState.value.copy(meaning = v) }
    fun onMeaningBurmeseChange(v: String) { _uiState.value = _uiState.value.copy(meaningBurmese = v) }
    fun onNsfwChange(v: Boolean) { _uiState.value = _uiState.value.copy(isNsfw = v) }

    fun onExampleChange(index: Int, value: String) {
        val list = _uiState.value.examples.toMutableList().also { it[index] = value }
        _uiState.value = _uiState.value.copy(examples = list)
    }

    fun addExample() {
        if (_uiState.value.examples.size < 5) {
            _uiState.value = _uiState.value.copy(examples = _uiState.value.examples + "")
        }
    }

    fun removeExample(index: Int) {
        val list = _uiState.value.examples.toMutableList().also { it.removeAt(index) }
        _uiState.value = _uiState.value.copy(examples = list)
    }

    fun submit() {
        val s = _uiState.value
        if (s.word.isBlank() || s.pronunciation.isBlank() || s.meaningBurmese.isBlank()) return
        viewModelScope.launch {
            _uiState.value = s.copy(isSubmitting = true, error = null)
            try {
                val user = authRepository.currentUserFlow.first()
                    ?: throw Exception("Please sign in to contribute")
                val isMod = user.role in listOf("moderator", "admin")
                val status = if (isMod) "approved" else "pending"
                val slug = s.word.trim().lowercase()
                    .replace(Regex("[^a-z0-9\\s-]"), "")
                    .replace(Regex("\\s+"), "-")
                val submission = SlangSubmission(
                    word = s.word.trim(),
                    slug = slug,
                    pronunciation = s.pronunciation.trim(),
                    meaning = s.meaning.trim().takeIf { it.isNotEmpty() } ?: "",
                    meaningBurmese = s.meaningBurmese.trim().takeIf { it.isNotEmpty() },
                    examples = s.examples.filter { it.isNotBlank() }.map { it.trim() },
                    isNsfw = s.isNsfw,
                    authorId = user.id,
                    authorName = user.displayName ?: user.email,
                    status = status,
                )
                contributeRepository.submitSlang(submission)
                _uiState.value = AddSlangUiState(submitted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = e.message)
            }
        }
    }

    fun resetForm() {
        _uiState.value = AddSlangUiState()
    }
}
