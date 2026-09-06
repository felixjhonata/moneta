package com.felixj.moneta.dashboard.model

import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.UiText

data class DashboardPageUiState(
    val currentBalance: UiText = UiText.Empty,
    val income: UiText = UiText.Empty,
    val expense: UiText = UiText.Empty,
    val recentActivities: List<ActivityItemUiModel> = emptyList(),
    val showSeeMoreButton: Boolean = false
)
