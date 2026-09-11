package com.felixj.moneta.dashboard.model

import com.felixj.moneta.R
import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.UiText

data class DashboardPageUiState(
    val currentBalance: UiText = UiText.StringResource(R.string.rp_value, "0"),
    val income: UiText = UiText.StringResource(R.string.rp_value, "0"),
    val expense: UiText = UiText.StringResource(R.string.rp_value, "0"),
    val recentActivities: List<ActivityItemUiModel> = emptyList(),
    val showSeeMoreButton: Boolean = false
)
