package com.bansagar.app.ui.init

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansagar.app.domain.repository.AuthRepository
import com.bansagar.app.domain.repository.SlangRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppInitViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val slangRepository: SlangRepository,
) : ViewModel() {

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    init {
        viewModelScope.launch {
            // Wait for the auth session to be restored from encrypted storage
            // (this is the main source of first-launch latency).
            // Also touch Room so the DB file is opened and WAL is set up.
            runCatching { authRepository.currentUserFlow.first() }
            runCatching { slangRepository.getCachedLatest(1) }
            _isInitializing.value = false
        }
    }
}
