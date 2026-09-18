package com.felixj.moneta.categories.model

import com.felixj.moneta.settings.model.CategoryUiModel

data class CategoriesPageUiState(
    val categories: List<CategoryUiModel> = emptyList()
)