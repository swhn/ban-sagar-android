package com.madebysai.bansagar.domain.repository

import android.content.Context
import com.madebysai.bansagar.data.model.AppUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserFlow: Flow<AppUser?>
    fun isSignedIn(): Boolean
    suspend fun signInWithGoogle(activityContext: Context): Result<AppUser>
    suspend fun signOut()
}
