package com.felixj.moneta.settings.model

data class SettingsPageUiState(
    val isDarkMode: Boolean = false,
    val currency: String = "$",
    val currencies: List<String> = emptyList()
)
