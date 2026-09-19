package com.felixj.moneta.categories.edit_category.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.categories.shared.model.AddCategoryPageUiEvent
import com.felixj.moneta.categories.shared.model.AddCategoryPageUiState
import com.felixj.moneta.categories.shared.model.AddCategoryPageUserEvent
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.repository.CategoryRepository
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

@HiltViewModel(assistedFactory = EditCategoryPageViewModel.Factory::class)
class EditCategoryPageViewModel @AssistedInject constructor(
    private val categoryRepository: CategoryRepository,
    @Assisted private val navKey: MonetaRoute.EditCategory
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddCategoryPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddCategoryPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: AddCategoryPageUserEvent) {
        when (userEvent) {
            AddCategoryPageUserEvent.LoadData -> loadCategory()
            AddCategoryPageUserEvent.NavigateBack -> viewModelScope.launch {
                _uiEvent.emit(AddCategoryPageUiEvent.NavigateBack)
            }
            AddCategoryPageUserEvent.Submit -> updateCategory()
            is AddCategoryPageUserEvent.UpdateName -> _uiState.update { it.copy(name = userEvent.name, nameError = false) }
            is AddCategoryPageUserEvent.SelectCategoryType -> _uiState.update { it.copy(categoryType = userEvent.categoryType) }
            is AddCategoryPageUserEvent.SelectIcon -> _uiState.update { it.copy(selectedIcon = userEvent.icon) }
        }
    }

    private fun loadCategory() {
        viewModelScope.launch {
            val category = categoryRepository.getCategory(navKey.categoryId) ?: return@launch
            _uiState.update {
                it.copy(
                    name = category.name,
                    categoryType = category.type,
                    selectedIcon = category.icon
                )
            }
        }
    }

    private fun updateCategory() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }

        viewModelScope.launch {
            val existing = categoryRepository.getCategory(navKey.categoryId) ?: return@launch
            categoryRepository.updateCategory(
                existing.copy(
                    name = state.name.trim(),
                    icon = state.selectedIcon,
                    type = state.categoryType
                )
            )
            _uiEvent.emit(AddCategoryPageUiEvent.NavigateBack)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: MonetaRoute.EditCategory): EditCategoryPageViewModel
    }
}