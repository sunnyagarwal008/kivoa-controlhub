package com.kivoa.controlhub.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.data.ThemeDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themeDataStore = ThemeDataStore(application)

    val theme: StateFlow<String> = themeDataStore.getTheme
        .map { it ?: "System" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "System"
        )

    fun setTheme(theme: String) {
        viewModelScope.launch {
            themeDataStore.setTheme(theme)
        }
    }
}