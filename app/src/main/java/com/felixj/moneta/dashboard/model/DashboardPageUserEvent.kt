package com.felixj.moneta.dashboard.model

import com.felixj.moneta.shared.view.BottomNavigationBarDestination

sealed interface DashboardPageUserEvent {
    data class BottomNavigationDestinationSelected(val destination: BottomNavigationBarDestination) : DashboardPageUserEvent
    data object SeeMoreButtonClick: DashboardPageUserEvent
}