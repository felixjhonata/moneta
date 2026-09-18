package com.felixj.moneta.categories.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felixj.moneta.R
import com.felixj.moneta.settings.model.CategoryUiModel
import com.felixj.moneta.shared.view.CategoriesGrid
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun CategoriesPage(modifier: Modifier = Modifier) {
    CategoriesPageContent(
        dummyCategories(),
        {},
        {},
        modifier
    )
}

@Composable
fun CategoriesPageContent(
    categories: List<CategoryUiModel>,
    onAddCategory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            item("page_title") {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onBack) {
                        Icon(
                            painterResource(R.drawable.baseline_arrow_back_24),
                            stringResource(R.string.back)
                        )
                    }

                    Text(
                        stringResource(R.string.categories),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(Modifier.height(12.dp))
            }

            item("category_grid") {
                CategoriesGrid(
                    categories,
                    Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    trailingCell = { AddCategoryCard(onAddCategory, it) }
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
    OutlinedCard(onClick = onClick, modifier = modifier) {
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

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun CategoriesPageContentPreview() {
    MonetaTheme {
        CategoriesPageContent(dummyCategories(), {}, {})
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
        CategoriesPageContent(dummyCategories(), {}, {})
    }
}

@Preview(
    name = "Empty",
    showSystemUi = true
)
@Composable
private fun EmptyCategoriesPageContentPreview() {
    MonetaTheme {
        CategoriesPageContent(emptyList(), {}, {})
    }
}

private fun dummyCategories() = listOf(
    CategoryUiModel(R.drawable.baseline_lightbulb_24, "Utilities", true),
    CategoryUiModel(R.drawable.baseline_fastfood_24, "Food", true),
    CategoryUiModel(R.drawable.baseline_directions_bus_24, "Transport", true),
    CategoryUiModel(R.drawable.baseline_account_balance_wallet_24, "Salary", false),
    CategoryUiModel(R.drawable.baseline_home_filled_24, "Home", true)
)