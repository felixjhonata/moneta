package com.felixj.moneta.activity_detail.model

sealed interface ActivityDetailPageUserEvent {
    data object LoadData : ActivityDetailPageUserEvent
    data object NavigateBack : ActivityDetailPageUserEvent
    data object EditClick : ActivityDetailPageUserEvent
    data object DeleteClick : ActivityDetailPageUserEvent
    data object ConfirmDelete : ActivityDetailPageUserEvent
    data object DismissDialog : ActivityDetailPageUserEvent
}