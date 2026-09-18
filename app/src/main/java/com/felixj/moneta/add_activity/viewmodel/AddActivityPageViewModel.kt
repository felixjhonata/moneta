package com.felixj.moneta.add_activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.add_activity.model.AddActivityCategoryUiModel
import com.felixj.moneta.add_activity.model.AddActivityPageUiEvent
import com.felixj.moneta.add_activity.model.AddActivityPageUiState
import com.felixj.moneta.add_activity.model.AddActivityPageUserEvent
import com.felixj.moneta.shared.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddActivityPageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddActivityPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddActivityPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: AddActivityPageUserEvent) {
        when (userEvent) {
            AddActivityPageUserEvent.LoadData -> loadCategories()
            AddActivityPageUserEvent.NavigateBack -> emitNavigateBack()
            is AddActivityPageUserEvent.UpdateAmount -> _uiState.update { it.copy(amount = userEvent.amount) }
            is AddActivityPageUserEvent.SelectActivityType -> _uiState.update { it.copy(activityType = userEvent.activityType) }
            is AddActivityPageUserEvent.SelectCategory -> _uiState.update { it.copy(selectedCategoryId = userEvent.categoryId) }
            is AddActivityPageUserEvent.UpdateDate -> _uiState.update { it.copy(date = userEvent.date) }
            is AddActivityPageUserEvent.UpdateTime -> _uiState.update { it.copy(time = userEvent.time) }
            is AddActivityPageUserEvent.UpdateNotes -> _uiState.update { it.copy(notes = userEvent.notes) }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = categoryRepository.getCategories().map {
                AddActivityCategoryUiModel(it.id, it.icon, it.name)
            }
            _uiState.update {
                it.copy(
                    categories = categories,
                    selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull()?.id
                )
            }
        }
    }

    private fun emitNavigateBack() {
        viewModelScope.launch {
            _uiEvent.emit(AddActivityPageUiEvent.NavigateBack)
        }
    }
}
