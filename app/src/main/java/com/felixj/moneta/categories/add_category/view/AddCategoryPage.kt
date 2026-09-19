package com.felixj.moneta.categories.add_category.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.felixj.moneta.R
import com.felixj.moneta.categories.add_category.viewmodel.AddCategoryPageViewModel
import com.felixj.moneta.categories.shared.model.AddCategoryPageUiEvent
import com.felixj.moneta.categories.shared.view.CategoryFormPageContent
import com.felixj.moneta.shared.util.goBack

@Composable
fun AddCategoryPage(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    viewModel: AddCategoryPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                AddCategoryPageUiEvent.NavigateBack -> backStack.goBack()
            }
        }
    }

    CategoryFormPageContent(
        headerRes = R.string.add_category,
        buttonRes = R.string.add_category,
        uiState = uiState,
        onUserEvent = viewModel::onUserEvent,
        modifier = modifier
    )
}