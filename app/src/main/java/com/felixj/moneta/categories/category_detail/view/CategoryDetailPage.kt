package com.felixj.moneta.categories.category_detail.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.felixj.moneta.R
import com.felixj.moneta.categories.category_detail.model.CategoryDetailPageDialog
import com.felixj.moneta.categories.category_detail.model.CategoryDetailPageUiEvent
import com.felixj.moneta.categories.category_detail.model.CategoryDetailPageUiState
import com.felixj.moneta.categories.category_detail.model.CategoryDetailPageUserEvent
import com.felixj.moneta.categories.category_detail.viewmodel.CategoryDetailPageViewModel
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.util.goBack
import com.felixj.moneta.shared.util.navigateTo
import com.felixj.moneta.shared.view.PageHeader
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun CategoryDetailPage(
    backStack: NavBackStack<NavKey>,
    viewModel: CategoryDetailPageViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.onUserEvent(CategoryDetailPageUserEvent.LoadData)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                CategoryDetailPageUiEvent.NavigateBack -> backStack.goBack()
                is CategoryDetailPageUiEvent.NavigateToEdit -> backStack.navigateTo(
                    MonetaRoute.EditCategory(uiEvent.categoryId)
                )
            }
        }
    }

    CategoryDetailPageContent(uiState, viewModel::onUserEvent, modifier)
}

@Composable
private fun CategoryDetailPageContent(
    uiState: CategoryDetailPageUiState,
    onUserEvent: (CategoryDetailPageUserEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = uiState.categoryType == CategoryType.EXPENSE

    Scaffold(modifier) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding
        ) {
            item {
                PageHeader(
                    stringResource(R.string.category_detail),
                    { onUserEvent(CategoryDetailPageUserEvent.NavigateBack) }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryDetail(
                        icon = uiState.icon,
                        name = uiState.name,
                        isExpense = isExpense
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow(R.string.category_name, uiState.name)
                        DetailRow(R.string.type, stringResource(if (isExpense) R.string.expense else R.string.income))
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        { onUserEvent(CategoryDetailPageUserEvent.EditClick) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.edit))
                    }

                    OutlinedButton(
                        { onUserEvent(CategoryDetailPageUserEvent.DeleteClick) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }

        when (uiState.dialog) {
            CategoryDetailPageDialog.None -> Unit
            CategoryDetailPageDialog.DeleteConfirmationDialog -> DeleteConfirmationDialog(
                onConfirm = { onUserEvent(CategoryDetailPageUserEvent.ConfirmDelete) },
                onDismiss = { onUserEvent(CategoryDetailPageUserEvent.DismissDialog) }
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.are_you_sure)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun CategoryDetail(
    icon: Int?,
    name: String,
    isExpense: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isExpense) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(icon ?: R.drawable.baseline_history_24),
                null,
                tint = if (isExpense) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            name,
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            stringResource(if (isExpense) R.string.expense else R.string.income),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun DetailRow(
    labelRes: Int,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun CategoryDetailPagePreview() {
    MonetaTheme {
        CategoryDetailPageContent(
            CategoryDetailPageViewModel.dummyUiState(),
            {}
        )
    }
}

@Preview(
    name = "Dark mode",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
private fun CategoryDetailPagePreviewDarkMode() {
    MonetaTheme {
        CategoryDetailPageContent(
            CategoryDetailPageViewModel.dummyUiState(),
            {}
        )
    }
}