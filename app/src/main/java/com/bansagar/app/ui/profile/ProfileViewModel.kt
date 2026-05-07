package com.bansagar.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansagar.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    val showNsfw: StateFlow<Boolean> = prefs.showNsfw.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false,
    )

    fun setShowNsfw(value: Boolean) {
        viewModelScope.launch { prefs.setShowNsfw(value) }
    }
}
