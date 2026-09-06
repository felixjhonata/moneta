package com.felixj.moneta.settings.model

import com.felixj.moneta.shared.view.BottomNavigationBarDestination

sealed interface SettingsPageUserEvent {
    data class BottomNavigationDestinationSelected(val destination: BottomNavigationBarDestination) : SettingsPageUserEvent
}