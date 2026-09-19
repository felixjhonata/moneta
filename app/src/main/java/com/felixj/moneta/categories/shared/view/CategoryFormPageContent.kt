package com.felixj.moneta.categories.shared.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felixj.moneta.R
import com.felixj.moneta.categories.shared.model.AddCategoryPageUiState
import com.felixj.moneta.categories.shared.model.AddCategoryPageUserEvent
import com.felixj.moneta.shared.view.CategoryTypeSelector
import com.felixj.moneta.shared.view.PageHeader
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun CategoryFormPageContent(
    uiState: AddCategoryPageUiState,
    onUserEvent: (AddCategoryPageUserEvent) -> Unit,
    modifier: Modifier = Modifier,
    headerRes: Int = R.string.add_category,
    buttonRes: Int = R.string.add_category
) {
    Scaffold(modifier) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            item("page_title") {
                PageHeader(
                    stringResource(headerRes),
                    { onUserEvent(AddCategoryPageUserEvent.NavigateBack) }
                )
            }

            item("name") {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { onUserEvent(AddCategoryPageUserEvent.UpdateName(it)) },
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    label = { Text(stringResource(R.string.category_name)) },
                    singleLine = true,
                    isError = uiState.nameError,
                    supportingText = if (uiState.nameError) {
                        { Text(stringResource(R.string.name_required)) }
                    } else {
                        null
                    }
                )
            }

            item("category_type") {
                CategoryTypeSelector(
                    selected = uiState.categoryType,
                    onSelect = { onUserEvent(AddCategoryPageUserEvent.SelectCategoryType(it)) },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            item("icon_label") {
                Text(
                    stringResource(R.string.select_icon),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item("icon_grid") {
                Column(
                    Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.iconOptions.chunked(5).forEach { icons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            icons.forEach { icon ->
                                IconCard(
                                    icon,
                                    icon == uiState.selectedIcon,
                                    { onUserEvent(AddCategoryPageUserEvent.SelectIcon(icon)) },
                                    Modifier.weight(1f)
                                )
                            }

                            repeat(5 - icons.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item("submit") {
                Button(
                    { onUserEvent(AddCategoryPageUserEvent.Submit) },
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(buttonRes))
                }
            }
        }
    }
}

@Composable
private fun IconCard(
    icon: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    ) {
        Icon(
            painterResource(icon),
            null,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterHorizontally)
        )
    }
}

internal fun dummyUiState() = AddCategoryPageUiState(
    name = "Food & Drinks"
)

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun CategoryFormPageContentPreview() {
    MonetaTheme {
        CategoryFormPageContent(dummyUiState(), {})
    }
}

@Preview(
    name = "Dark mode",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
private fun CategoryFormPageContentPreviewDarkMode() {
    MonetaTheme {
        CategoryFormPageContent(dummyUiState(), {})
    }
}