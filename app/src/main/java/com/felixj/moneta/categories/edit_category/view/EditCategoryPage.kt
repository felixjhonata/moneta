package com.felixj.moneta.categories.edit_category.view

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
import com.felixj.moneta.categories.edit_category.viewmodel.EditCategoryPageViewModel
import com.felixj.moneta.categories.shared.model.AddCategoryPageUiEvent
import com.felixj.moneta.categories.shared.model.AddCategoryPageUserEvent
import com.felixj.moneta.categories.shared.view.CategoryFormPageContent
import com.felixj.moneta.categories.shared.view.dummyUiState
import com.felixj.moneta.shared.util.goBack
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun EditCategoryPage(
    backStack: NavBackStack<NavKey>,
    viewModel: EditCategoryPageViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.onUserEvent(AddCategoryPageUserEvent.LoadData)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                AddCategoryPageUiEvent.NavigateBack -> backStack.goBack()
            }
        }
    }

    CategoryFormPageContent(
        headerRes = R.string.edit_category,
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
private fun EditCategoryPagePreview() {
    MonetaTheme {
        CategoryFormPageContent(
            headerRes = R.string.edit_category,
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
private fun EditCategoryPagePreviewDarkMode() {
    MonetaTheme {
        CategoryFormPageContent(
            headerRes = R.string.edit_category,
            buttonRes = R.string.save,
            uiState = dummyUiState(),
            onUserEvent = {}
        )
    }
}