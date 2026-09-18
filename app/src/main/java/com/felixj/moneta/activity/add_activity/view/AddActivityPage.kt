package com.felixj.moneta.activity.add_activity.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.felixj.moneta.R
import com.felixj.moneta.activity.add_activity.viewmodel.AddActivityPageViewModel
import com.felixj.moneta.activity.shared.model.AddActivityPageUiEvent
import com.felixj.moneta.activity.shared.model.AddActivityPageUserEvent
import com.felixj.moneta.activity.shared.view.ActivityFormPageContent
import com.felixj.moneta.shared.util.goBack

@Composable
fun AddActivityPage(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    viewModel: AddActivityPageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                AddActivityPageUiEvent.NavigateBack -> backStack.goBack()
            }
        }
    }

    ActivityFormPageContent(
        headerRes = R.string.add_activity,
        buttonRes = R.string.add_activity,
        uiState = uiState,
        onUserEvent = viewModel::onUserEvent,
        modifier = modifier
    )
}