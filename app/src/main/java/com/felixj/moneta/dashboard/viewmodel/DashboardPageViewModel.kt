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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardPageViewModel @Inject constructor() : ViewModel() {
    companion object {
        fun dummyUiState() = DashboardPageUiState(
            UiText.StringResource(R.string.rp_value, "4.850.000"),
            UiText.StringResource(R.string.rp_value, "1.500.000"),
            UiText.StringResource(R.string.rp_value, "800.000"),
            listOf(
                ActivityItemUiModel(
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Electricity Bills"),
                    UiText.DynamicString("12 April 2026"),
                    UiText.DynamicString("-Rp 1.200.000"),
                    true
                ),
                ActivityItemUiModel(
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Water Bills"),
                    UiText.DynamicString("12 April 2026"),
                    UiText.DynamicString("-Rp 800.000"),
                    true
                ),
                ActivityItemUiModel(
                    R.drawable.baseline_account_balance_wallet_24,
                    UiText.DynamicString("Salary"),
                    UiText.DynamicString("10 April 2026"),
                    UiText.DynamicString("+Rp 2.000.000"),
                    false
                )
            ),
            true
        )
    }

    private val _uiState = MutableStateFlow(
//        DashboardPageUiState() TODO: Uncomment
        dummyUiState() // TODO: Remove
    )
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DashboardPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: DashboardPageUserEvent) {
        when (userEvent) {
            is DashboardPageUserEvent.NavigateTo -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateTo(MonetaRoute.History)
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
}