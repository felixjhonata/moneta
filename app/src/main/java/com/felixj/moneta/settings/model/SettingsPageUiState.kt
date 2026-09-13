package com.felixj.moneta.settings.model

data class SettingsPageUiState(
    val categories: List<CategoryUiModel> = emptyList(),
    val useSystemTheme: Boolean = true,
    val isDarkMode: Boolean = false,
    val currency: Currency = Currency.IDR,
    val currencies: List<Currency> = Currency.entries,
    val currencyDropdownExpanded: Boolean = false,
    val showSeeMoreButton: Boolean = false
)
