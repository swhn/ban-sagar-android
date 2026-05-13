package com.bansagar.app.data.repository

import android.content.Context
import android.util.Log
import com.bansagar.app.BuildConfig
import com.bansagar.app.data.model.AppUser
import com.bansagar.app.domain.repository.AuthRepository
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

private const val TAG = "AuthRepository"

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
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            return Result.failure(IllegalStateException(
                "GOOGLE_WEB_CLIENT_ID is missing in local.properties"
            ))
        }
        return try {
            val credentialManager = CredentialManager.create(activityContext)

            // Two-pass: first try only previously-authorized accounts (silent UX),
            // then fall back to the full account picker.
            val idToken = runCatching {
                getIdToken(credentialManager, activityContext, filterByAuthorized = true)
            }.getOrElse { firstErr ->
                if (firstErr is NoCredentialException) {
                    getIdToken(credentialManager, activityContext, filterByAuthorized = false)
                } else {
                    throw firstErr
                }
            }

            client.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            val userId = client.auth.currentSessionOrNull()?.user?.id
                ?: return Result.failure(Exception("Sign-in failed: no session after IDToken exchange"))

            val profile = ensureProfile(userId)

            // Upload current FCM token so notifications work immediately after sign-in.
            // onNewToken fires on install before the user is authenticated, so the token
            // would otherwise only be stored on the next token rotation.
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                client.from("users").update(buildJsonObject { put("fcm_token", fcmToken) }) {
                    filter { eq("id", userId) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not upload FCM token after sign-in", e)
            }

            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogle failed", e)
            Result.failure(e)
        }
    }

    private suspend fun getIdToken(
        credentialManager: CredentialManager,
        activityContext: Context,
        filterByAuthorized: Boolean,
    ): String {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorized)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(filterByAuthorized)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credResult = credentialManager.getCredential(activityContext, request)
        return GoogleIdTokenCredential.createFrom(credResult.credential.data).idToken
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
        try { client.from("users").insert(newUser) } catch (e: Exception) {
            Log.w(TAG, "Could not insert new user profile", e)
        }
        return newUser
    }

    override suspend fun signOut() {
        try { client.auth.signOut() } catch (e: Exception) {
            Log.w(TAG, "signOut failed", e)
        }
    }
}
