package com.madebysai.bansagar.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madebysai.bansagar.data.model.AppUser
import com.madebysai.bansagar.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<AppUser?> = authRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isSignedIn: StateFlow<Boolean> = authRepository.currentUserFlow
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun signIn(activityContext: Context, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activityContext)
            onResult(result.isSuccess, result.exceptionOrNull()?.message)
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
