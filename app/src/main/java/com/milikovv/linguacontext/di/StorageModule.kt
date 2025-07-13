package com.milikovv.linguacontext.di

import android.content.Context
import com.milikovv.linguacontext.utils.DataStoreManager
import com.milikovv.linguacontext.utils.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserSettings {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context, CoroutineScope(Dispatchers.Default + Job()))
    }

    @Provides
    @Singleton
    fun provideUserSettings(dataStoreManager: DataStoreManager): Flow<Settings> {
        return dataStoreManager.effectiveSettings
    }
}
