package com.milikovv.linguacontext.di

import com.milikovv.linguacontext.utils.DataStoreManager
import com.milikovv.linguacontext.utils.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugBaseUrl

/**
 * Simplified [UserSettings] to avoid Android API access.
 */
@Module
@InstallIn(SingletonComponent::class)
object DebugSettingsModule {
    @DebugBaseUrl
    @Provides
    @Singleton
    fun provideDebugBaseUrl(): Flow<Settings> = flow {
        emit(Settings("http://localhost:11434", "qwen3:32b"))
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UserSettings {
    @UserBaseUrl
    @Provides
    @Singleton
    fun provideUserSettings(dataStoreManager: DataStoreManager): Flow<Settings> {
        return dataStoreManager.settingsFlow
    }
}
