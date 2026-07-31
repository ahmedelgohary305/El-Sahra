package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elsahra.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean? = null,
    val isInitialized: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = userPreferencesRepository.isDarkMode
        .map { isDarkMode ->
            SettingsUiState(isDarkMode = isDarkMode, isInitialized = true)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsUiState()
        )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }
}
