package com.felixj.moneta.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.settings.model.SettingsPageUiEvent
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsPageViewModel @Inject constructor() : ViewModel() {
    private val _uiEvent = MutableSharedFlow<SettingsPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: SettingsPageUserEvent) {
        when (userEvent) {
            is SettingsPageUserEvent.BottomNavigationDestinationSelected -> {
                when (userEvent.destination) {
                    BottomNavigationBarDestination.Settings -> Unit
                    BottomNavigationBarDestination.Dashboard -> {
                        viewModelScope.launch {
                            _uiEvent.emit(SettingsPageUiEvent.NavigateTo(MonetaRoute.Dashboard, true))
                        }
                    }
                    BottomNavigationBarDestination.History -> {
                        viewModelScope.launch {
                            _uiEvent.emit(SettingsPageUiEvent.NavigateTo(MonetaRoute.History, true))
                        }
                    }
                }
            }
        }
    }
}