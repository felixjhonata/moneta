package com.felixj.moneta.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.dashboard.model.DashboardPageUiEvent
import com.felixj.moneta.dashboard.model.DashboardPageUiEvent.NavigateTo
import com.felixj.moneta.dashboard.model.DashboardPageUiState
import com.felixj.moneta.dashboard.model.DashboardPageUserEvent
import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.repository.ActivityRepository
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

private const val MAX_RECENT_ACTIVITY = 3

@HiltViewModel
class DashboardPageViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DashboardPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: DashboardPageUserEvent) {
        when (userEvent) {
            DashboardPageUserEvent.LoadData -> {
                viewModelScope.launch {
                    val recentActivities = activityRepository.getActivities(MAX_RECENT_ACTIVITY)
                    val currentBalance = activityRepository.getCurrentBalance()
                    val (startOfMonth, startOfNextMonth) = DateUtil.getLocalMonthAsUtcRange()
                    val earnedThisMonth = activityRepository.getTotalAmountByTypeAndDateRange(
                        CategoryType.INCOME,
                        startOfMonth,
                        startOfNextMonth
                    )
                    val spentThisMonth = activityRepository.getTotalAmountByTypeAndDateRange(
                        CategoryType.EXPENSE,
                        startOfMonth,
                        startOfNextMonth
                    )

                    val recentActivityUiModels = recentActivities.map { item ->
                        ActivityItemUiModel(
                            item.activity.id,
                            item.categoryIcon,
                            UiText.DynamicString(item.activity.name),
                            UiText.DynamicString(DateUtil.formatForDisplay(item.activity.date)),
                            UiText.CurrencyAmount(
                                amount = item.activity.amount,
                                prefix = if (item.categoryType == CategoryType.EXPENSE) "- " else "+ "
                            ),
                            item.categoryType == CategoryType.EXPENSE
                        )
                    }

                    _uiState.update {
                        it.copy(
                            currentBalance = UiText.CurrencyAmount(
                                amount = currentBalance,
                                prefix = if (currentBalance < 0) "- " else ""
                            ),
                            income = UiText.CurrencyAmount(earnedThisMonth),
                            expense = UiText.CurrencyAmount(spentThisMonth),
                            recentActivities = recentActivityUiModels,
                            showSeeMoreButton = recentActivities.isNotEmpty()
                        )
                    }
                }
            }

            is DashboardPageUserEvent.NavigateTo -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateTo(userEvent.destination)
                    )
                }
            }

            DashboardPageUserEvent.SeeMoreButtonClick -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateTo(MonetaRoute.History)
                    )
                }
            }

            is DashboardPageUserEvent.ActivityItemClick -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateTo(MonetaRoute.ActivityDetail(userEvent.activityId))
                    )
                }
            }
        }
    }

    companion object {
        fun dummyUiState() = DashboardPageUiState(
            UiText.CurrencyAmount(4850000),
            UiText.CurrencyAmount(1500000),
            UiText.CurrencyAmount(800000),
            listOf(
                ActivityItemUiModel(
                    1,
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Electricity Bills"),
                    UiText.DynamicString("12 April 2026"),
                    UiText.CurrencyAmount(1200000, "- "),
                    true
                ),
                ActivityItemUiModel(
                    2,
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Water Bills"),
                    UiText.DynamicString("12 April 2026"),
                    UiText.CurrencyAmount(800000, "- "),
                    true
                ),
                ActivityItemUiModel(
                    3,
                    R.drawable.baseline_account_balance_wallet_24,
                    UiText.DynamicString("Salary"),
                    UiText.DynamicString("10 April 2026"),
                    UiText.CurrencyAmount(2000000, "+ "),
                    false
                )
            ),
            true
        )
    }
}
