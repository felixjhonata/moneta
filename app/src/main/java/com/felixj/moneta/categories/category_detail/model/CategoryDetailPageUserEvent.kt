package com.felixj.moneta.categories.category_detail.model

sealed interface CategoryDetailPageUserEvent {
    data object LoadData : CategoryDetailPageUserEvent
    data object NavigateBack : CategoryDetailPageUserEvent
    data object EditClick : CategoryDetailPageUserEvent
    data object DeleteClick : CategoryDetailPageUserEvent
    data object ConfirmDelete : CategoryDetailPageUserEvent
    data object DismissDialog : CategoryDetailPageUserEvent
}