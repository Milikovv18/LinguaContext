package com.milikovv.linguacontext.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milikovv.linguacontext.utils.DataStoreManager
import com.milikovv.linguacontext.utils.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for MainActivity that observes [DataStoreManager] and allows user to change settings
 * from inside the app.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    val storeManager: DataStoreManager
) : ViewModel() {
    val storageState: StateFlow<Settings> = storeManager.effectiveSettings

    fun updateBaseUrl(url: String) {
        viewModelScope.launch {
            storeManager.saveBaseUrl(url)
        }
    }

    fun updateModelName(name: String) {
        viewModelScope.launch {
            storeManager.saveModelName(name)
        }
    }

    fun updateThinkMode(enabled: Boolean) {
        viewModelScope.launch {
            storeManager.saveThinkMode(enabled)
        }
    }
}
