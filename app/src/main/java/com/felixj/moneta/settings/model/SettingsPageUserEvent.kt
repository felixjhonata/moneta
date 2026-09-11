package com.felixj.moneta.settings.model

import com.felixj.moneta.shared.model.MonetaRoute

sealed interface SettingsPageUserEvent {
    data class NavigateTo(val destination: MonetaRoute) : SettingsPageUserEvent
}