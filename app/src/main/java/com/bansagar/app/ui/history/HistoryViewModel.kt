package com.bansagar.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansagar.app.data.model.Slang
import com.bansagar.app.domain.repository.AuthRepository
import com.bansagar.app.domain.repository.ContributeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val slangs: List<Slang> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val filterStatus: String? = null,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val contributeRepository: ContributeRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = authRepository.currentUserFlow.first()
                if (user == null) {
                    _uiState.value = HistoryUiState(isLoading = false)
                    return@launch
                }
                val slangs = contributeRepository.getUserHistory(user.id)
                _uiState.value = _uiState.value.copy(slangs = slangs, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun setFilter(status: String?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
    }
}
