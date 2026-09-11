package com.felixj.moneta.settings.model

import com.felixj.moneta.shared.model.MonetaRoute

sealed interface SettingsPageUserEvent {
    data object LoadData: SettingsPageUserEvent
    data class NavigateTo(val destination: MonetaRoute) : SettingsPageUserEvent
    data class ToggleCurrencyDropdown(val isExpanded: Boolean): SettingsPageUserEvent
    data class SelectCurrency(val currency: Currency): SettingsPageUserEvent
    data class ToggleDarkMode(val isOn: Boolean): SettingsPageUserEvent
}