package com.felixj.moneta.categories.category_detail.model

import androidx.annotation.DrawableRes
import com.felixj.moneta.shared.room.entity.CategoryType

data class CategoryDetailPageUiState(
    @DrawableRes val icon: Int? = null,
    val name: String = "",
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val dialog: CategoryDetailPageDialog = CategoryDetailPageDialog.None
)