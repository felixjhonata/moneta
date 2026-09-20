package com.felixj.moneta.activity.add_activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.activity.shared.model.AddActivityCategoryUiModel
import com.felixj.moneta.activity.shared.model.AddActivityPageDialog
import com.felixj.moneta.activity.shared.model.AddActivityPageUiEvent
import com.felixj.moneta.activity.shared.model.AddActivityPageUiState
import com.felixj.moneta.activity.shared.model.AddActivityPageUserEvent
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddActivityPageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        run {
            val now = Calendar.getInstance()
            AddActivityPageUiState(
                date = DateUtil.currentDateInput(now),
                time = DateUtil.currentTimeInput(now)
            )
        }
    )
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddActivityPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: AddActivityPageUserEvent) {
        when (userEvent) {
            AddActivityPageUserEvent.LoadData -> loadCategories()
            AddActivityPageUserEvent.NavigateBack -> emitNavigateBack()
            AddActivityPageUserEvent.Submit -> submitActivity()
            is AddActivityPageUserEvent.UpdateAmount -> _uiState.update { it.copy(amount = userEvent.amount, amountError = false) }
            is AddActivityPageUserEvent.SelectCategoryType -> _uiState.update { state ->
                state.copy(
                    categoryType = userEvent.categoryType,
                    selectedCategoryId = state.categories.firstOrNull { it.type == userEvent.categoryType }?.id,
                    categoryError = false
                )
            }
            is AddActivityPageUserEvent.SelectCategory -> _uiState.update { it.copy(selectedCategoryId = userEvent.categoryId, categoryError = false) }
            is AddActivityPageUserEvent.UpdateDate -> _uiState.update { it.copy(date = userEvent.date, dateError = false) }
            is AddActivityPageUserEvent.UpdateTime -> _uiState.update { it.copy(time = userEvent.time, timeError = false) }
            is AddActivityPageUserEvent.UpdateNotes -> _uiState.update { it.copy(note = userEvent.notes) }
            AddActivityPageUserEvent.ShowDatePicker -> _uiState.update { it.copy(dialog = AddActivityPageDialog.DatePickerDialog) }
            AddActivityPageUserEvent.ShowTimePicker -> _uiState.update { it.copy(dialog = AddActivityPageDialog.TimePickerDialog) }
            AddActivityPageUserEvent.DismissDialog -> _uiState.update { it.copy(dialog = AddActivityPageDialog.None) }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = categoryRepository.getCategories().map {
                AddActivityCategoryUiModel(it.id, it.icon, it.name, it.type)
            }
            _uiState.update {
                it.copy(
                    categories = categories,
                    selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull { category -> category.type == it.categoryType }?.id
                )
            }
        }
    }

    private fun emitNavigateBack() {
        viewModelScope.launch {
            _uiEvent.emit(AddActivityPageUiEvent.NavigateBack)
        }
    }

    private fun submitActivity() {
        val state = _uiState.value
        val amountValid = state.amount.toLongOrNull()?.let { it > 0 } == true
        val categoryValid = state.selectedCategoryId != null
        val dateValid = DateUtil.isValidDateInput(state.date)
        val timeValid = DateUtil.isValidTimeInput(state.time)

        _uiState.update {
            it.copy(
                amountError = !amountValid,
                categoryError = !categoryValid,
                dateError = !dateValid,
                timeError = !timeValid
            )
        }
        if (!(amountValid && categoryValid && dateValid && timeValid)) return

        val category = state.categories.firstOrNull { it.id == state.selectedCategoryId } ?: return
        val storedDateTime = DateUtil.toIsoUtcDateTime(state.date, state.time) ?: return

        viewModelScope.launch {
            val id = activityRepository.getNextActivityId()
            activityRepository.insertActivity(
                Activity(
                    id = id,
                    categoryId = category.id,
                    name = category.label,
                    date = storedDateTime,
                    amount = state.amount.toLong(),
                    notes = state.note
                )
            )
            _uiEvent.emit(AddActivityPageUiEvent.NavigateBack)
        }
    }
}
