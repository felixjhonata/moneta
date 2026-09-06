package com.felixj.moneta.settings.model

import com.felixj.moneta.shared.model.MonetaRoute

sealed interface SettingsPageUiEvent {
    data class NavigateTo(val destination: MonetaRoute, val clearBackStack: Boolean = false): SettingsPageUiEvent
}