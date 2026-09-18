package com.felixj.moneta.add_activity.model

import com.felixj.moneta.shared.room.entity.CategoryType

data class AddActivityPageUiState(
    val amount: String = "",
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val categories: List<AddActivityCategoryUiModel> = emptyList(),
    val selectedCategoryId: Int? = null,
    val date: String = "",
    val time: String = "",
    val notes: String = "",
    val dialog: AddActivityPageDialog = AddActivityPageDialog.None
)
