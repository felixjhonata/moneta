package com.felixj.moneta.dashboard.model

data class ActivityItemUiModel(
    val activityLabel: String,
    val activityDate: String,
    val amount: String,
    val isExpense: Boolean
)
