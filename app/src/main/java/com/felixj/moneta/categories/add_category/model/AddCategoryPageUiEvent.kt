package com.felixj.moneta.categories.add_category.model

sealed interface AddCategoryPageUiEvent {
    data object NavigateBack : AddCategoryPageUiEvent
}