package com.felixj.moneta.add_activity.model

sealed interface AddActivityPageUiEvent {
    data object NavigateBack : AddActivityPageUiEvent
}
