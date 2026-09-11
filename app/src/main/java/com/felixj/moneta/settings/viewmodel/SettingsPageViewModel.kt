package com.felixj.moneta.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.settings.model.CategoryUiModel
import com.felixj.moneta.settings.model.SettingsPageUiEvent
import com.felixj.moneta.settings.model.SettingsPageUiState
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsPageViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SettingsPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: SettingsPageUserEvent) {
        when (userEvent) {
            is SettingsPageUserEvent.NavigateTo -> {
                viewModelScope.launch {
                    _uiEvent.emit(SettingsPageUiEvent.NavigateTo(userEvent.destination))
                }
            }
        }
    }

    companion object {
        fun dummyUiState(isDarkMode: Boolean = false) = SettingsPageUiState(
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
            currency = "IDR (Rp)",
            isDarkMode = isDarkMode,
            currencies = listOf("USD ($)", "IDR (Rp)"),
            showSeeMoreButton = true
        )
    }
}