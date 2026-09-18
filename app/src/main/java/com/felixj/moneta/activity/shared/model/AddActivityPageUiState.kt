package com.felixj.moneta.activity.shared.model

import com.felixj.moneta.shared.room.entity.CategoryType

data class AddActivityPageUiState(
    val amount: String = "",
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val categories: List<AddActivityCategoryUiModel> = emptyList(),
    val selectedCategoryId: Int? = null,
    val date: String = "",
    val time: String = "",
    val note: String = "",
    val dialog: AddActivityPageDialog = AddActivityPageDialog.None,
    val amountError: Boolean = false,
    val categoryError: Boolean = false,
    val dateError: Boolean = false,
    val timeError: Boolean = false
)
