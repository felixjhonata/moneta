package com.felixj.moneta.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.dashboard.model.DashboardPageUiEvent
import com.felixj.moneta.dashboard.model.DashboardPageUserEvent
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardPageViewModel @Inject constructor(): ViewModel() {
    private val _uiEvent = MutableSharedFlow<DashboardPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: DashboardPageUserEvent) {
        when (userEvent) {
            is DashboardPageUserEvent.BottomNavigationDestinationSelected -> {
                when (userEvent.destination) {
                    BottomNavigationBarDestination.Dashboard -> Unit
                    BottomNavigationBarDestination.History -> {
                        viewModelScope.launch {
                            _uiEvent.emit(DashboardPageUiEvent.NavigateTo(MonetaRoute.History, true))
                        }
                    }
                    BottomNavigationBarDestination.Settings -> {
                        viewModelScope.launch {
                            _uiEvent.emit(DashboardPageUiEvent.NavigateTo(MonetaRoute.Settings, true))
                        }
                    }
                }
            }
        }
    }
}