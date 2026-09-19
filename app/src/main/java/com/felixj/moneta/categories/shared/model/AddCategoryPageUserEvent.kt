package com.felixj.moneta.categories.shared.model

import androidx.annotation.DrawableRes
import com.felixj.moneta.shared.room.entity.CategoryType

sealed interface AddCategoryPageUserEvent {
    data object LoadData : AddCategoryPageUserEvent
    data object NavigateBack : AddCategoryPageUserEvent
    data object Submit : AddCategoryPageUserEvent
    data class UpdateName(val name: String) : AddCategoryPageUserEvent
    data class SelectCategoryType(val categoryType: CategoryType) : AddCategoryPageUserEvent
    data class SelectIcon(@DrawableRes val icon: Int) : AddCategoryPageUserEvent
}