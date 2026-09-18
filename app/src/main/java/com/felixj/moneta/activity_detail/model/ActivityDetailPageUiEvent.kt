package com.felixj.moneta.activity_detail.model

sealed interface ActivityDetailPageUiEvent {
    data object NavigateBack : ActivityDetailPageUiEvent
}