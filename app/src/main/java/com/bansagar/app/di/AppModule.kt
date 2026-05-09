package com.bansagar.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.bansagar.app.BuildConfig
import com.bansagar.app.data.local.AppDatabase
import com.bansagar.app.data.local.SlangDao
import com.bansagar.app.data.repository.AuthRepositoryImpl
import com.bansagar.app.data.repository.ContributeRepositoryImpl
import com.bansagar.app.data.repository.LeaderboardRepositoryImpl
import com.bansagar.app.data.repository.SiteSettingsRepositoryImpl
import com.bansagar.app.data.repository.SlangRepositoryImpl
import com.bansagar.app.data.repository.UserRepositoryImpl
import com.bansagar.app.data.repository.VoteRepositoryImpl
import com.bansagar.app.domain.repository.AuthRepository
import com.bansagar.app.domain.repository.ContributeRepository
import com.bansagar.app.domain.repository.LeaderboardRepository
import com.bansagar.app.domain.repository.SiteSettingsRepository
import com.bansagar.app.domain.repository.SlangRepository
import com.bansagar.app.domain.repository.UserRepository
import com.bansagar.app.domain.repository.VoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            defaultSerializer = KotlinXSerializer(Json {
                coerceInputValues = true
                ignoreUnknownKeys = true
            })
            install(Postgrest)
            install(Auth)
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "ban_sagar.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideSlangDao(db: AppDatabase): SlangDao = db.slangDao()

    @Provides
    @Singleton
    fun provideSlangRepository(client: SupabaseClient, dao: SlangDao): SlangRepository {
        return SlangRepositoryImpl(client, dao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(client: SupabaseClient): AuthRepository {
        return AuthRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideUserRepository(client: SupabaseClient): UserRepository {
        return UserRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideVoteRepository(client: SupabaseClient): VoteRepository {
        return VoteRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideContributeRepository(client: SupabaseClient): ContributeRepository {
        return ContributeRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideSiteSettingsRepository(client: SupabaseClient): SiteSettingsRepository {
        return SiteSettingsRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideLeaderboardRepository(client: SupabaseClient): LeaderboardRepository {
        return LeaderboardRepositoryImpl(client)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_prefs") },
        )
}
