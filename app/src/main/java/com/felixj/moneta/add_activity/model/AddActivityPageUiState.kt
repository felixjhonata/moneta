package com.felixj.moneta.add_activity.model

import com.felixj.moneta.shared.room.entity.ActivityType

data class AddActivityPageUiState(
    val amount: String = "",
    val activityType: ActivityType = ActivityType.INCOME,
    val categories: List<AddActivityCategoryUiModel> = emptyList(),
    val selectedCategoryId: Int? = null,
    val date: String = "",
    val time: String = "",
    val notes: String = ""
)
