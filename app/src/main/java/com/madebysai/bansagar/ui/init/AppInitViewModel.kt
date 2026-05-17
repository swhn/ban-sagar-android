package com.madebysai.bansagar.ui.init

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madebysai.bansagar.data.model.SiteSettings
import com.madebysai.bansagar.domain.repository.AuthRepository
import com.madebysai.bansagar.domain.repository.SiteSettingsRepository
import com.madebysai.bansagar.domain.repository.SlangRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
    private val siteSettingsRepository: SiteSettingsRepository,
) : ViewModel() {

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _siteSettings = MutableStateFlow(SiteSettings())
    val siteSettings: StateFlow<SiteSettings> = _siteSettings.asStateFlow()

    init {
        viewModelScope.launch {
            val settingsDeferred = async {
                runCatching { siteSettingsRepository.getSettings() }.getOrDefault(SiteSettings())
            }
            runCatching { authRepository.currentUserFlow.first() }
            runCatching { slangRepository.getCachedLatest(1) }
            _siteSettings.value = settingsDeferred.await()
            _isInitializing.value = false
        }
    }
}
