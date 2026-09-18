package com.felixj.moneta.activity.edit_activity.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.felixj.moneta.R
import com.felixj.moneta.activity.edit_activity.viewmodel.EditActivityPageViewModel
import com.felixj.moneta.activity.shared.model.AddActivityPageUiEvent
import com.felixj.moneta.activity.shared.model.AddActivityPageUserEvent
import com.felixj.moneta.activity.shared.view.ActivityFormPageContent
import com.felixj.moneta.activity.shared.view.dummyUiState
import com.felixj.moneta.shared.util.goBack
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun EditActivityPage(
    backStack: NavBackStack<NavKey>,
    viewModel: EditActivityPageViewModel,
    modifier: Modifier = Modifier
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
        headerRes = R.string.edit_activity,
        buttonRes = R.string.save,
        uiState = uiState,
        onUserEvent = viewModel::onUserEvent,
        modifier = modifier
    )
}

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun EditActivityPagePreview() {
    MonetaTheme {
        ActivityFormPageContent(
            headerRes = R.string.edit_activity,
            buttonRes = R.string.save,
            uiState = dummyUiState(),
            onUserEvent = {}
        )
    }
}

@Preview(
    name = "Dark mode",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
private fun EditActivityPagePreviewDarkMode() {
    MonetaTheme {
        ActivityFormPageContent(
            headerRes = R.string.edit_activity,
            buttonRes = R.string.save,
            uiState = dummyUiState(),
            onUserEvent = {}
        )
    }
}