package com.felixj.moneta.dashboard.model

data class DashboardPageUiState(
    val currentBalance: String,
    val income: String,
    val expense: String,
    val recentActivities: List<ActivityItemUiModel>
)
