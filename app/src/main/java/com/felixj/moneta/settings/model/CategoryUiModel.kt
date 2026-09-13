package com.felixj.moneta.settings.model

import androidx.annotation.DrawableRes
import com.felixj.moneta.R

data class CategoryUiModel(
    @param:DrawableRes val icon: Int = R.drawable.baseline_lightbulb_24,
    val label: String = "",
    val isExpense: Boolean = true
)