package com.felixj.moneta.activity_detail.model

import androidx.annotation.DrawableRes
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.room.entity.CategoryType

data class ActivityDetailPageUiState(
    @DrawableRes val icon: Int? = null,
    val name: String = "",
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val amount: UiText.CurrencyAmount = UiText.CurrencyAmount(0),
    val date: String = "",
    val time: String = "",
    val note: String = "",
    val dialog: ActivityDetailPageDialog = ActivityDetailPageDialog.None
)