package com.felixj.moneta.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.history.model.HistoryListItemUiModel
import com.felixj.moneta.history.model.HistoryPageUiEvent
import com.felixj.moneta.history.model.HistoryPageUiState
import com.felixj.moneta.history.model.HistoryPageUserEvent
import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryPageViewModel @Inject constructor(): ViewModel() {
    companion object {
        fun dummyUiState() = HistoryPageUiState(
            listOf(
                HistoryListItemUiModel.Date("Today"),
                HistoryListItemUiModel.ActivityItem(
                    ActivityItemUiModel(
                        R.drawable.baseline_lightbulb_24,
                        UiText.DynamicString("Electricity Bills"),
                        UiText.DynamicString("4 September 2026"),
                        UiText.DynamicString("-Rp 1.200.000"),
                        true,
                    )
                ),
                HistoryListItemUiModel.ActivityItem(
                    ActivityItemUiModel(
                        R.drawable.baseline_lightbulb_24,
                        UiText.DynamicString("Water Bills"),
                        UiText.DynamicString("4 September 2026"),
                        UiText.DynamicString("-Rp 800.000"),
                        true,
                    )
                ),
                HistoryListItemUiModel.ActivityItem(
                    ActivityItemUiModel(
                        R.drawable.baseline_account_balance_wallet_24,
                        UiText.DynamicString("Salary"),
                        UiText.DynamicString("4 September 2026"),
                        UiText.DynamicString("+Rp 13.000.000"),
                        false,
                    )
                ),
                HistoryListItemUiModel.Date("Yesterday"),
                HistoryListItemUiModel.ActivityItem(
                    ActivityItemUiModel(
                        R.drawable.baseline_lightbulb_24,
                        UiText.DynamicString("Phone Bills"),
                        UiText.DynamicString("3 September 2026"),
                        UiText.DynamicString("-Rp 100.000"),
                        true
                    )
                )
            )
        )
    }

    private val _uiState = MutableStateFlow(
//        HistoryPageUiState() TODO: Uncomment
        dummyUiState() // TODO: Remove
    )
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HistoryPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: HistoryPageUserEvent) {
        when (userEvent) {
            is HistoryPageUserEvent.BottomNavigationDestinationSelected -> {
                when (userEvent.destination) {
                    BottomNavigationBarDestination.History -> Unit
                    BottomNavigationBarDestination.Dashboard -> {
                        viewModelScope.launch {
                            _uiEvent.emit(HistoryPageUiEvent.NavigateTo(MonetaRoute.Dashboard, true))
                        }
                    }
                    BottomNavigationBarDestination.Settings -> {
                        viewModelScope.launch {
                            _uiEvent.emit(HistoryPageUiEvent.NavigateTo(MonetaRoute.Settings, true))
                        }
                    }
                }
            }
        }
    }
}