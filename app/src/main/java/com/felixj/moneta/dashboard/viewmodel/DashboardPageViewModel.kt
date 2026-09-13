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
import com.felixj.moneta.shared.room.entity.ActivityType
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
                    _uiState.update {
                        it.copy(
                            recentActivities = activityRepository.getActivities(MAX_RECENT_ACTIVITY).map { activity ->
                                ActivityItemUiModel(
                                    R.drawable.baseline_lightbulb_24,
                                    UiText.DynamicString(activity.name),
                                    UiText.DynamicString(activity.date),
                                    UiText.CurrencyAmount(
                                        amount = activity.amount,
                                        prefix = if (activity.type == ActivityType.EXPENSE) "- " else "+ "
                                    ),
                                    activity.type == ActivityType.EXPENSE
                                )
                            }
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
        }
    }

    companion object {
        fun dummyUiState() = DashboardPageUiState(
            UiText.CurrencyAmount(4850000),
            UiText.CurrencyAmount(1500000),
            UiText.CurrencyAmount(800000),
            listOf(
                ActivityItemUiModel(
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Electricity Bills"),
                    UiText.DynamicString("12 April 2026"),
                    UiText.CurrencyAmount(1200000, "- "),
                    true
                ),
                ActivityItemUiModel(
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Water Bills"),
                    UiText.DynamicString("12 April 2026"),
                    UiText.CurrencyAmount(800000, "- "),
                    true
                ),
                ActivityItemUiModel(
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