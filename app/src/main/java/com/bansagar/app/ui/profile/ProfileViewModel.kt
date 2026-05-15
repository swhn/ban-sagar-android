package com.bansagar.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansagar.app.data.model.AppUser
import com.bansagar.app.data.model.UserStats
import com.bansagar.app.data.preferences.ThemeMode
import com.bansagar.app.data.preferences.UserPreferencesRepository
import com.bansagar.app.domain.repository.AuthRepository
import com.bansagar.app.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: AppUser? = null,
    val stats: UserStats = UserStats(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val displayNameInput: String = "",
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val showNsfw: StateFlow<Boolean> = prefs.showNsfw.stateIn(
        viewModelScope, SharingStarted.Eagerly, false,
    )
    val themeMode: StateFlow<ThemeMode> = prefs.themeMode.stateIn(
        viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM,
    )
    val wotdNotifications: StateFlow<Boolean> = prefs.wotdNotifications.stateIn(
        viewModelScope, SharingStarted.Eagerly, true,
    )

    init {
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    displayNameInput = user?.displayName ?: "",
                    isLoading = false,
                )
                if (user != null) {
                    loadStats(user.id)
                    prefs.setShowNsfw(user.showNsfw)
                }
            }
        }
    }

    private fun loadStats(userId: String) {
        viewModelScope.launch {
            val stats = userRepository.getStats(userId)
            _uiState.value = _uiState.value.copy(stats = stats)
        }
    }

    fun onDisplayNameChange(name: String) {
        _uiState.value = _uiState.value.copy(displayNameInput = name)
    }

    fun saveDisplayName() {
        val user = _uiState.value.user ?: return
        val name = _uiState.value.displayNameInput.trim()
        if (name.isEmpty() || name == user.displayName) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                userRepository.updateDisplayName(user.id, name)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    user = user.copy(displayName = name),
                    successMessage = "Display name updated",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setWotdNotifications(enabled: Boolean) {
        viewModelScope.launch { prefs.setWotdNotifications(enabled) }
        val topic = WOTD_TOPIC
        if (enabled) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
    }

    fun setShowNsfw(value: Boolean) {
        viewModelScope.launch {
            prefs.setShowNsfw(value)
            val user = _uiState.value.user ?: return@launch
            try {
                userRepository.updatePreferences(
                    userId = user.id,
                    showNsfw = value,
                    notifyApproved = user.notifyApproved,
                    notifyBadges = user.notifyBadges,
                )
                _uiState.value = _uiState.value.copy(user = user.copy(showNsfw = value))
            } catch (_: Exception) { }
        }
    }

    fun toggleNotifyApproved(value: Boolean) = updatePrefs { it.copy(notifyApproved = value) }
    fun toggleNotifyBadges(value: Boolean) = updatePrefs { it.copy(notifyBadges = value) }

    private fun updatePrefs(transform: (AppUser) -> AppUser) {
        val user = _uiState.value.user ?: return
        val updated = transform(user)
        viewModelScope.launch {
            try {
                userRepository.updatePreferences(
                    userId = user.id,
                    showNsfw = updated.showNsfw,
                    notifyApproved = updated.notifyApproved,
                    notifyBadges = updated.notifyBadges,
                )
                _uiState.value = _uiState.value.copy(user = updated)
            } catch (_: Exception) { }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }

    private companion object {
        const val WOTD_TOPIC = "word_of_the_day"
    }
}
