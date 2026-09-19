package com.felixj.moneta.categories.root.model

sealed interface CategoriesPageUiEvent {
    data object NavigateBack : CategoriesPageUiEvent
    data object NavigateToAddCategory : CategoriesPageUiEvent
    data class NavigateToCategoryDetail(val categoryId: Int) : CategoriesPageUiEvent
}