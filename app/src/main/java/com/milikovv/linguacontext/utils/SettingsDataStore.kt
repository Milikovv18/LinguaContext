package com.milikovv.linguacontext.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Extension property to create DataStore instance
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        // Key for storing base URL string
        private val BASE_URL_KEY = stringPreferencesKey("base_url")

        // Default base URL if none saved
        private const val DEFAULT_BASE_URL = ""
    }

    // Flow to observe base URL changes, emits current or default value
    val baseUrlFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            // Handle exceptions, e.g. IOException when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[BASE_URL_KEY] ?: DEFAULT_BASE_URL
        }

    // Suspend function to save/update base URL
    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[BASE_URL_KEY] = url
        }
    }
}