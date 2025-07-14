package com.milikovv.linguacontext.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.io.IOException
import javax.inject.Inject

// Extension property to create DataStore instance
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * In-app settings container saved to shared preferences
 */
data class Settings(
    val baseUrl: String = "127.0.0.1:11434",
    val modelName: String = "qwen3:32b",
    val thinkDisable: Boolean = true
)

data class SettingsOverrides(
    val thinkDisable: Boolean = false
)

/**
 * [preferencesDataStore] manager. Provides methods to change preferences' values and default values.
 */
class DataStoreManager @Inject constructor(
    private val context: Context,
    dataStoreScope: CoroutineScope
) {
    // Flow to observe base URL changes, emits current or default value
    private val settingsFlow: Flow<Settings> = context.dataStore.data
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
                modelName = preferences[LLM_KEY] ?: "qwen3:32b",
                thinkDisable = preferences[THINK_DISABLED_KEY] == true
            )
        }

    // Temporary overrides (nullable or with default values)
    private val _temporaryOverrides = MutableStateFlow<SettingsOverrides?>(null)
    private val temporaryOverrides = _temporaryOverrides.asStateFlow()

    private val combinedSettingsFlow: Flow<Settings> = combine(
        settingsFlow,
        temporaryOverrides
    ) { persistent, temp ->
        Settings(
            baseUrl = persistent.baseUrl,
            modelName = persistent.modelName,
            thinkDisable = temp?.thinkDisable ?: persistent.thinkDisable
        )
    }

    val effectiveSettings: StateFlow<Settings> = combinedSettingsFlow
        .stateIn(
            scope = dataStoreScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

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

    // Suspend function to save/update think mode
    suspend fun saveThinkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THINK_DISABLED_KEY] = enabled
        }
    }

    // Per-request settings overriding
    fun overrideSettings(skipThinking: Boolean) {
        _temporaryOverrides.update { settings ->
            if (skipThinking)
                SettingsOverrides(thinkDisable = true)
            else
                null
        }
    }


    companion object {
        // Keys for storing base URL string and LLM name
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
        private val LLM_KEY = stringPreferencesKey("model")
        private val THINK_DISABLED_KEY = booleanPreferencesKey("think_disabled")
    }
}