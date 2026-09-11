package com.felixj.moneta.settings.model

import com.felixj.moneta.shared.model.MonetaRoute

sealed interface SettingsPageUiEvent {
    data class NavigateTo(val destination: MonetaRoute): SettingsPageUiEvent
}