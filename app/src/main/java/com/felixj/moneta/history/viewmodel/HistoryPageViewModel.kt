package com.felixj.moneta.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.history.model.HistoryPageUiEvent
import com.felixj.moneta.history.model.HistoryPageUserEvent
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryPageViewModel @Inject constructor(): ViewModel() {
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