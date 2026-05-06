package com.bansagar.app.data.repository

import android.content.Context
import com.bansagar.app.BuildConfig
import com.bansagar.app.data.model.AppUser
import com.bansagar.app.domain.repository.AuthRepository
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
) : AuthRepository {

    override val currentUserFlow: Flow<AppUser?> = client.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val userId = status.session.user?.id ?: return@map null
                try {
                    client.from("users").select {
                        filter { eq("id", userId) }
                        limit(1)
                    }.decodeSingleOrNull<AppUser>()
                } catch (_: Exception) { null }
            }
            else -> null
        }
    }

    override fun isSignedIn(): Boolean = client.auth.currentSessionOrNull() != null

    override suspend fun signInWithGoogle(activityContext: Context): Result<AppUser> {
        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credResult = credentialManager.getCredential(activityContext, request)
            val idToken = GoogleIdTokenCredential.createFrom(credResult.credential.data).idToken

            client.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            val userId = client.auth.currentSessionOrNull()?.user?.id
                ?: return Result.failure(Exception("Sign-in failed: no session"))

            Result.success(ensureProfile(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureProfile(userId: String): AppUser {
        val existing = try {
            client.from("users").select {
                filter { eq("id", userId) }
                limit(1)
            }.decodeSingleOrNull<AppUser>()
        } catch (_: Exception) { null }

        if (existing != null) return existing

        val supabaseUser = client.auth.currentUserOrNull()
        val meta = supabaseUser?.userMetadata
        val displayName = meta?.get("full_name")?.toString()?.trim('"')
        val avatarUrl = meta?.get("avatar_url")?.toString()?.trim('"')

        val newUser = AppUser(
            id = userId,
            email = supabaseUser?.email ?: "",
            displayName = displayName,
            avatarUrl = avatarUrl,
        )
        try { client.from("users").insert(newUser) } catch (_: Exception) { }
        return newUser
    }

    override suspend fun signOut() {
        try { client.auth.signOut() } catch (_: Exception) { }
    }
}
