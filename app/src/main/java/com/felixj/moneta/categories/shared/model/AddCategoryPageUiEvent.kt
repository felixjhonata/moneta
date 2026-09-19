package com.felixj.moneta.categories.shared.model

sealed interface AddCategoryPageUiEvent {
    data object NavigateBack : AddCategoryPageUiEvent
}