package com.felixj.moneta.shared.model

data class ActivityItemUiModel(
    val activityLabel: UiText,
    val activityDate: UiText,
    val amount: UiText,
    val isExpense: Boolean
)