package com.bansagar.app.domain.repository

import android.content.Context
import com.bansagar.app.data.model.AppUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserFlow: Flow<AppUser?>
    fun isSignedIn(): Boolean
    suspend fun signInWithGoogle(activityContext: Context): Result<AppUser>
    suspend fun signOut()
}
