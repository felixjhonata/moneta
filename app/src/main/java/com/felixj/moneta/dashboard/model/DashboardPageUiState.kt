package com.felixj.moneta.dashboard.model

import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.UiText

data class DashboardPageUiState(
    val currentBalance: UiText,
    val income: UiText,
    val expense: UiText,
    val recentActivities: List<ActivityItemUiModel>
)
