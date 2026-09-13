package com.felixj.moneta.dashboard.model

import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.UiText

data class DashboardPageUiState(
    val currentBalance: UiText = UiText.CurrencyAmount(0),
    val income: UiText = UiText.CurrencyAmount(0),
    val expense: UiText = UiText.CurrencyAmount(0),
    val recentActivities: List<ActivityItemUiModel> = emptyList(),
    val showSeeMoreButton: Boolean = false
)
