package com.felixj.moneta.categories.model

sealed interface CategoriesPageUiEvent {
    data object NavigateBack : CategoriesPageUiEvent
    data object NavigateToAddCategory : CategoriesPageUiEvent
}