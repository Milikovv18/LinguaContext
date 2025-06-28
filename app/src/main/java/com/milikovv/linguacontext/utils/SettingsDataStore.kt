package com.milikovv.linguacontext.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

// Extension property to create DataStore instance
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * In-app settings container saved to shared preferences
 */
data class Settings(
    val baseUrl: String = "127.0.0.1:11434",
    val modelName: String = "qwen3:32b"
)

/**
 * [preferencesDataStore] manager. Provides methods to change preferences' values and default values.
 */
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Flow to observe base URL changes, emits current or default value
    val settingsFlow: Flow<Settings> = context.dataStore.data
        .catch { exception ->
            // Handle exceptions, e.g. IOException when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            Settings(
                baseUrl = preferences[BASE_URL_KEY] ?: "127.0.0.1:11434",
                modelName = preferences[LLM_KEY] ?: "qwen3:32b"
            )
        }

    // Suspend function to save/update base URL
    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[BASE_URL_KEY] = url
        }
    }

    // Suspend function to save/update model name
    suspend fun saveModelName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[LLM_KEY] = name
        }
    }


    companion object {
        // Keys for storing base URL string and LLM name
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
        private val LLM_KEY = stringPreferencesKey("model")
    }
}