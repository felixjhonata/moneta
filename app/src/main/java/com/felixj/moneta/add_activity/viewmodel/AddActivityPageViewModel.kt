package com.felixj.moneta.add_activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.add_activity.model.AddActivityCategoryUiModel
import com.felixj.moneta.add_activity.model.AddActivityPageDialog
import com.felixj.moneta.add_activity.model.AddActivityPageUiEvent
import com.felixj.moneta.add_activity.model.AddActivityPageUiState
import com.felixj.moneta.add_activity.model.AddActivityPageUserEvent
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.util.DateUtil
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
    private val categoryRepository: CategoryRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddActivityPageUiState())
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

    companion object {
        fun dummyUiState() = AddActivityPageUiState(
            amount = "1200000",
            categoryType = CategoryType.EXPENSE,
            categories = listOf(
                AddActivityCategoryUiModel(1, R.drawable.baseline_fastfood_24, "Food & Drinks", CategoryType.EXPENSE),
                AddActivityCategoryUiModel(2, R.drawable.baseline_directions_bus_24, "Transport", CategoryType.EXPENSE),
                AddActivityCategoryUiModel(3, R.drawable.baseline_lightbulb_24, "Utilities", CategoryType.EXPENSE),
                AddActivityCategoryUiModel(4, R.drawable.baseline_access_time_24, "Entertainment", CategoryType.EXPENSE),
                AddActivityCategoryUiModel(5, R.drawable.baseline_history_24, "Shopping", CategoryType.EXPENSE),
                AddActivityCategoryUiModel(6, R.drawable.baseline_account_balance_wallet_24, "Salary", CategoryType.INCOME),
                AddActivityCategoryUiModel(7, R.drawable.baseline_directions_bus_24, "Deposit", CategoryType.INCOME),
                AddActivityCategoryUiModel(8, R.drawable.baseline_lightbulb_24, "Investment", CategoryType.INCOME),
                AddActivityCategoryUiModel(9, R.drawable.baseline_fastfood_24, "Gift", CategoryType.INCOME),
                AddActivityCategoryUiModel(10, R.drawable.baseline_access_time_24, "Refund", CategoryType.INCOME)
            ),
            selectedCategoryId = 3,
            date = "18092026",
            time = "1400",
            note = "Makan di luar dengan keluarga"
        )
    }
}
