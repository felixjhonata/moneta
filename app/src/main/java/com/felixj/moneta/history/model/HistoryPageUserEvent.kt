package com.felixj.moneta.history.model

import com.felixj.moneta.shared.model.MonetaRoute

sealed interface HistoryPageUserEvent {
    data object LoadData: HistoryPageUserEvent
    data class NavigateTo(val destination: MonetaRoute) : HistoryPageUserEvent
}