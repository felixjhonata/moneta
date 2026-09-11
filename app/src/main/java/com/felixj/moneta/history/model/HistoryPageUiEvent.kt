package com.felixj.moneta.history.model

import com.felixj.moneta.shared.model.MonetaRoute

sealed interface HistoryPageUiEvent {
    data class NavigateTo(val destination: MonetaRoute): HistoryPageUiEvent
}