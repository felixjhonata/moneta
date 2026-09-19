package com.felixj.moneta.categories.add_category.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.categories.add_category.model.AddCategoryPageUiEvent
import com.felixj.moneta.categories.add_category.model.AddCategoryPageUiState
import com.felixj.moneta.categories.add_category.model.AddCategoryPageUserEvent
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.room.entity.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCategoryPageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddCategoryPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddCategoryPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: AddCategoryPageUserEvent) {
        when (userEvent) {
            AddCategoryPageUserEvent.NavigateBack -> emitNavigateBack()
            AddCategoryPageUserEvent.Submit -> submitCategory()
            is AddCategoryPageUserEvent.UpdateName -> _uiState.update { it.copy(name = userEvent.name, nameError = false) }
            is AddCategoryPageUserEvent.SelectCategoryType -> _uiState.update { it.copy(categoryType = userEvent.categoryType) }
            is AddCategoryPageUserEvent.SelectIcon -> _uiState.update { it.copy(selectedIcon = userEvent.icon) }
        }
    }

    private fun submitCategory() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }

        viewModelScope.launch {
            val id = categoryRepository.getNextCategoryId()
            categoryRepository.insertCategory(
                Category(
                    id = id,
                    name = state.name.trim(),
                    icon = state.selectedIcon,
                    type = state.categoryType
                )
            )
            _uiEvent.emit(AddCategoryPageUiEvent.NavigateBack)
        }
    }

    private fun emitNavigateBack() {
        viewModelScope.launch {
            _uiEvent.emit(AddCategoryPageUiEvent.NavigateBack)
        }
    }

    companion object {
        fun dummyUiState() = AddCategoryPageUiState(name = "Food & Drinks")
    }
}