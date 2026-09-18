package com.felixj.moneta.activity.shared.model

import androidx.annotation.DrawableRes
import com.felixj.moneta.shared.room.entity.CategoryType

data class AddActivityCategoryUiModel(
    val id: Int,
    @param:DrawableRes val icon: Int,
    val label: String,
    val type: CategoryType
)
