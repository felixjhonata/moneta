package com.felixj.moneta.categories.category_detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.categories.category_detail.model.CategoryDetailPageDialog
import com.felixj.moneta.categories.category_detail.model.CategoryDetailPageUiEvent
import com.felixj.moneta.categories.category_detail.model.CategoryDetailPageUiState
import com.felixj.moneta.categories.category_detail.model.CategoryDetailPageUserEvent
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.room.entity.CategoryType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CategoryDetailPageViewModel.Factory::class)
class CategoryDetailPageViewModel @AssistedInject constructor(
    private val categoryRepository: CategoryRepository,
    @Assisted private val navKey: MonetaRoute.CategoryDetail
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryDetailPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CategoryDetailPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: CategoryDetailPageUserEvent) {
        when (userEvent) {
            CategoryDetailPageUserEvent.LoadData -> loadCategory()
            CategoryDetailPageUserEvent.NavigateBack -> viewModelScope.launch {
                _uiEvent.emit(CategoryDetailPageUiEvent.NavigateBack)
            }
            CategoryDetailPageUserEvent.EditClick -> viewModelScope.launch {
                _uiEvent.emit(CategoryDetailPageUiEvent.NavigateToEdit(navKey.categoryId))
            }
            CategoryDetailPageUserEvent.DeleteClick -> _uiState.update {
                it.copy(dialog = CategoryDetailPageDialog.DeleteConfirmationDialog)
            }
            CategoryDetailPageUserEvent.DismissDialog -> _uiState.update {
                it.copy(dialog = CategoryDetailPageDialog.None)
            }
            CategoryDetailPageUserEvent.ConfirmDelete -> viewModelScope.launch {
                val category = categoryRepository.getCategory(navKey.categoryId) ?: return@launch
                categoryRepository.deleteCategory(category)
                _uiState.update { it.copy(dialog = CategoryDetailPageDialog.None) }
                _uiEvent.emit(CategoryDetailPageUiEvent.NavigateBack)
            }
        }
    }

    private fun loadCategory() {
        viewModelScope.launch {
            val category = categoryRepository.getCategory(navKey.categoryId) ?: return@launch
            _uiState.update {
                it.copy(
                    icon = category.icon,
                    name = category.name,
                    categoryType = category.type
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: MonetaRoute.CategoryDetail): CategoryDetailPageViewModel
    }

    companion object {
        fun dummyUiState() = CategoryDetailPageUiState(
            icon = R.drawable.baseline_lightbulb_24,
            name = "Utilities",
            categoryType = CategoryType.EXPENSE
        )
    }
}