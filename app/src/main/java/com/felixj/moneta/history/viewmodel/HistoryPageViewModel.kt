package com.felixj.moneta.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.history.model.HistoryListItemUiModel
import com.felixj.moneta.history.model.HistoryPageUiEvent
import com.felixj.moneta.history.model.HistoryPageUiState
import com.felixj.moneta.history.model.HistoryPageUserEvent
import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.room.entity.ActivityType
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
class HistoryPageViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(HistoryPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HistoryPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: HistoryPageUserEvent) {
        when (userEvent) {
            HistoryPageUserEvent.LoadData -> {
                viewModelScope.launch {
                    val activities = activityRepository.getActivities()
                    val sortedActivities = activities.sortedByDescending { it.activity.date }
                    val grouped = sortedActivities.groupBy { item ->
                        DateUtil.formatRelativeDate(item.activity.date)
                    }
                    val historyListItems = grouped.flatMap { (header, items) ->
                        listOf(HistoryListItemUiModel.Date(header)) + items.map { item ->
                            HistoryListItemUiModel.ActivityItem(
                                ActivityItemUiModel(
                                    icon = item.categoryIcon,
                                    activityLabel = UiText.DynamicString(item.activity.name),
                                    activityDate = UiText.DynamicString(DateUtil.formatForDisplay(item.activity.date)),
                                    amount = UiText.CurrencyAmount(
                                        amount = item.activity.amount,
                                        prefix = if (item.activity.type == ActivityType.EXPENSE) "- " else "+ "
                                    ),
                                    isExpense = item.activity.type == ActivityType.EXPENSE
                                )
                            )
                        }
                    }
                    _uiState.update {
                        it.copy(historyListItems = historyListItems)
                    }
                }
            }
            is HistoryPageUserEvent.NavigateTo -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        HistoryPageUiEvent.NavigateTo(userEvent.destination))
                }
            }
        }
    }

    companion object {
        fun dummyUiState() = HistoryPageUiState(
            listOf(
                HistoryListItemUiModel.Date("Today"),
                HistoryListItemUiModel.ActivityItem(
                    ActivityItemUiModel(
                        R.drawable.baseline_lightbulb_24,
                        UiText.DynamicString("Electricity Bills"),
                        UiText.DynamicString("4 September 2026"),
                        UiText.CurrencyAmount(1200000, "- "),
                        true,
                    )
                ),
                HistoryListItemUiModel.ActivityItem(
                    ActivityItemUiModel(
                        R.drawable.baseline_lightbulb_24,
                        UiText.DynamicString("Water Bills"),
                        UiText.DynamicString("4 September 2026"),
                        UiText.CurrencyAmount(800000, "- "),
                        true,
                    )
                ),
                HistoryListItemUiModel.ActivityItem(
                    ActivityItemUiModel(
                        R.drawable.baseline_account_balance_wallet_24,
                        UiText.DynamicString("Salary"),
                        UiText.DynamicString("4 September 2026"),
                        UiText.CurrencyAmount(13000000, "+ "),
                        false,
                    )
                ),
                HistoryListItemUiModel.Date("Yesterday"),
                HistoryListItemUiModel.ActivityItem(
                    ActivityItemUiModel(
                        R.drawable.baseline_lightbulb_24,
                        UiText.DynamicString("Phone Bills"),
                        UiText.DynamicString("3 September 2026"),
                        UiText.CurrencyAmount(100000, "- "),
                        true
                    )
                )
            )
        )
    }
}