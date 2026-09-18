package com.felixj.moneta.activity.shared.model

sealed interface AddActivityPageUiEvent {
    data object NavigateBack : AddActivityPageUiEvent
}
