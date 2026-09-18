package com.felixj.moneta.activity.activity_detail.model

sealed interface ActivityDetailPageUiEvent {
    data object NavigateBack : ActivityDetailPageUiEvent
    data class NavigateToEdit(val activityId: Int) : ActivityDetailPageUiEvent
}