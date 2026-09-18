package com.felixj.moneta.activity.shared.model

import com.felixj.moneta.shared.room.entity.CategoryType

sealed interface AddActivityPageUserEvent {
    data object LoadData : AddActivityPageUserEvent
    data object NavigateBack : AddActivityPageUserEvent
    data object Submit : AddActivityPageUserEvent
    data class UpdateAmount(val amount: String) : AddActivityPageUserEvent
    data class SelectCategoryType(val categoryType: CategoryType) : AddActivityPageUserEvent
    data class SelectCategory(val categoryId: Int) : AddActivityPageUserEvent
    data class UpdateDate(val date: String) : AddActivityPageUserEvent
    data class UpdateTime(val time: String) : AddActivityPageUserEvent
    data class UpdateNotes(val notes: String) : AddActivityPageUserEvent
    data object ShowDatePicker : AddActivityPageUserEvent
    data object ShowTimePicker : AddActivityPageUserEvent
    data object DismissDialog : AddActivityPageUserEvent
}
