package com.felixj.moneta.categories.model

sealed interface CategoriesPageUserEvent {
    data object LoadData : CategoriesPageUserEvent
    data object NavigateBack : CategoriesPageUserEvent
}