package com.felixj.moneta.settings.model

import androidx.annotation.DrawableRes

data class CategoryUiModel(
    @param:DrawableRes val icon: Int,
    val label: String,
    val isExpense: Boolean
)