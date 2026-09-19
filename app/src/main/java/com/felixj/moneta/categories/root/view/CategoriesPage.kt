package com.felixj.moneta.categories.root.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.felixj.moneta.R
import com.felixj.moneta.categories.root.model.CategoriesPageUiEvent
import com.felixj.moneta.categories.root.model.CategoriesPageUiState
import com.felixj.moneta.categories.root.model.CategoriesPageUserEvent
import com.felixj.moneta.categories.root.viewmodel.CategoriesPageViewModel
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.util.goBack
import com.felixj.moneta.shared.util.navigateTo
import com.felixj.moneta.shared.view.CategoriesGrid
import com.felixj.moneta.shared.view.PageHeader
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun CategoriesPage(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    viewModel: CategoriesPageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onUserEvent(CategoriesPageUserEvent.LoadData)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                CategoriesPageUiEvent.NavigateBack -> backStack.goBack()
                CategoriesPageUiEvent.NavigateToAddCategory -> backStack.navigateTo(MonetaRoute.AddCategory)
                is CategoriesPageUiEvent.NavigateToCategoryDetail -> backStack.navigateTo(
                    MonetaRoute.CategoryDetail(uiEvent.categoryId)
                )
            }
        }
    }

    CategoriesPageContent(
        uiState,
        viewModel::onUserEvent,
        modifier
    )
}

@Composable
fun CategoriesPageContent(
    uiState: CategoriesPageUiState,
    onUserEvent: (CategoriesPageUserEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            item("page_title") {
                PageHeader(
                    stringResource(R.string.categories),
                    { onUserEvent(CategoriesPageUserEvent.NavigateBack) }
                )

                Spacer(Modifier.height(12.dp))
            }

            item("category_grid") {
                CategoriesGrid(
                    uiState.categories,
                    Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    trailingCell = { AddCategoryCard({ onUserEvent(CategoriesPageUserEvent.NavigateToAddCategory) }, it) },
                    onCategoryClick = {
                        onUserEvent(CategoriesPageUserEvent.NavigateToCategoryDetail(it.id))
                    }
                )
            }
        }
    }
}

@Composable
private fun AddCategoryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CardDefaults.shape
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Card(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(),
        shape = shape,
        modifier = modifier.dashedBorder(borderColor, 12.dp, 8.dp, 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    painterResource(R.drawable.baseline_add_24),
                    stringResource(R.string.add_category),
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Text(
                stringResource(R.string.add_category),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    onLength: Dp,
    offLength: Dp
) = drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(onLength.toPx(), offLength.toPx()))
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun CategoriesPageContentPreview() {
    MonetaTheme {
        CategoriesPageContent(CategoriesPageViewModel.dummyUiState(), {})
    }
}

@Preview(
    name = "Dark Mode",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
private fun CategoriesPageContentDarkModePreview() {
    MonetaTheme {
        CategoriesPageContent(CategoriesPageViewModel.dummyUiState(), {})
    }
}

@Preview(
    name = "Empty",
    showSystemUi = true
)
@Composable
private fun EmptyCategoriesPageContentPreview() {
    MonetaTheme {
        CategoriesPageContent(CategoriesPageUiState(emptyList()), {})
    }
}