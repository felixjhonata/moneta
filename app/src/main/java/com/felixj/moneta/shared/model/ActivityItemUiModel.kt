package com.felixj.moneta.shared.model

data class ActivityItemUiModel(
    val activityLabel: String,
    val activityDate: String,
    val amount: String,
    val isExpense: Boolean
)