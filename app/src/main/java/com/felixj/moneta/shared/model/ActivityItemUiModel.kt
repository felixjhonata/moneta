package com.felixj.moneta.shared.model

import androidx.annotation.DrawableRes

data class ActivityItemUiModel(
    val activityId: Int,
    @param:DrawableRes val icon: Int,
    val activityLabel: UiText,
    val activityDate: UiText,
    val amount: UiText,
    val isExpense: Boolean
)