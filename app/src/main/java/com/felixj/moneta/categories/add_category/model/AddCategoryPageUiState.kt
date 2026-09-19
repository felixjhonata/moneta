package com.felixj.moneta.categories.add_category.model

import com.felixj.moneta.R
import com.felixj.moneta.shared.room.entity.CategoryType

val addCategoryIconOptions: List<Int> = listOf(
    R.drawable.baseline_lightbulb_24,
    R.drawable.baseline_home_filled_24,
    R.drawable.baseline_tv_24,
    R.drawable.baseline_fastfood_24,
    R.drawable.baseline_face_retouching_natural_24,
    R.drawable.baseline_directions_bus_24,
    R.drawable.baseline_account_balance_wallet_24,
    R.drawable.outline_show_chart_24,
    R.drawable.baseline_oil_barrel_24,
    R.drawable.baseline_settings_24,
    R.drawable.baseline_more_horiz_24
)

data class AddCategoryPageUiState(
    val name: String = "",
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val iconOptions: List<Int> = addCategoryIconOptions,
    val selectedIcon: Int = addCategoryIconOptions.first(),
    val nameError: Boolean = false
)