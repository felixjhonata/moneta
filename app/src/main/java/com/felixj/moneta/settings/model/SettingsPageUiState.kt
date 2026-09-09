package com.felixj.moneta.settings.model

data class SettingsPageUiState(
    val categories: List<CategoryUiModel> = emptyList(),
    val isDarkMode: Boolean = false,
    val currency: String = "$",
    val currencies: List<String> = emptyList(),
    val showSeeMoreButton: Boolean = false
)
