package com.felixj.moneta.categories.category_detail.model

sealed interface CategoryDetailPageUiEvent {
    data object NavigateBack : CategoryDetailPageUiEvent
    data class NavigateToEdit(val categoryId: Int) : CategoryDetailPageUiEvent
}