package com.felixj.moneta.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.settings.model.CategoryUiModel
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.settings.model.SettingsPageUiEvent
import com.felixj.moneta.settings.model.SettingsPageUiEvent.*
import com.felixj.moneta.settings.model.SettingsPageUiState
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.shared.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsPageViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SettingsPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: SettingsPageUserEvent) {
        when (userEvent) {
            SettingsPageUserEvent.LoadData -> {
                val usingSystemTheme = userPreferencesRepository.isUsingSystemTheme()
                val isDark = if (usingSystemTheme) false else (userPreferencesRepository.getDarkModePreference() ?: false)
                _uiState.update {
                    it.copy(
                        useSystemTheme = usingSystemTheme,
                        isDarkMode = isDark,
                        currency = userPreferencesRepository.getCurrency()
                    )
                }
            }
            is SettingsPageUserEvent.NavigateTo -> {
                viewModelScope.launch {
                    _uiEvent.emit(NavigateTo(userEvent.destination))
                }
            }
            is SettingsPageUserEvent.ToggleCurrencyDropdown -> {
                _uiState.update { it.copy(currencyDropdownExpanded = userEvent.isExpanded) }
            }
            is SettingsPageUserEvent.ToggleUseSystemTheme -> {
                userPreferencesRepository.setUseSystemTheme(userEvent.isOn)
                _uiState.update {
                    it.copy(
                        useSystemTheme = userEvent.isOn,
                        isDarkMode = false
                    )
                }
            }
            is SettingsPageUserEvent.ToggleDarkMode -> {
                userPreferencesRepository.setDarkMode(userEvent.isOn)
                _uiState.update { it.copy(isDarkMode = userEvent.isOn) }
            }
            is SettingsPageUserEvent.SelectCurrency -> {
                userPreferencesRepository.setCurrency(userEvent.currency)
                _uiState.update { it.copy(currencyDropdownExpanded = false, currency = userEvent.currency) }
            }
        }
    }

    companion object {
        fun dummyUiState(isDarkMode: Boolean = false, useSystemTheme: Boolean = true) = SettingsPageUiState(
            categories = listOf(
                CategoryUiModel(
                    R.drawable.baseline_lightbulb_24,
                    "Utilities",
                    true
                ),
                CategoryUiModel(
                    R.drawable.baseline_fastfood_24,
                    "Food",
                    true
                ),
                CategoryUiModel(
                    R.drawable.baseline_directions_bus_24,
                    "Transport",
                    true
                ),
                CategoryUiModel(
                    R.drawable.baseline_account_balance_wallet_24,
                    "Salary",
                    false
                )
            ),
            currency = Currency.IDR,
            isDarkMode = isDarkMode,
            currencies = Currency.entries,
            showSeeMoreButton = true
        )
    }
}