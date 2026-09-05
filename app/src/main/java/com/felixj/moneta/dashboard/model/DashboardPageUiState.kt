package com.felixj.moneta.dashboard.model

import com.felixj.moneta.shared.model.ActivityItemUiModel

data class DashboardPageUiState(
    val currentBalance: String,
    val income: String,
    val expense: String,
    val recentActivities: List<ActivityItemUiModel>
)
