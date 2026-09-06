package com.felixj.moneta.history.model

import com.felixj.moneta.shared.view.BottomNavigationBarDestination

sealed interface HistoryPageUserEvent {
    data class BottomNavigationDestinationSelected(val destination: BottomNavigationBarDestination) : HistoryPageUserEvent
}