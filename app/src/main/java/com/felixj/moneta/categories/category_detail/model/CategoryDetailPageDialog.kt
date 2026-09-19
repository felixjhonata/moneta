package com.felixj.moneta.categories.category_detail.model

sealed interface CategoryDetailPageDialog {
    data object None : CategoryDetailPageDialog
    data object DeleteConfirmationDialog : CategoryDetailPageDialog
}