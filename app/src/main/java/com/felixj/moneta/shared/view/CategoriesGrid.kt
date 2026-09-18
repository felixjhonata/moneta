package com.felixj.moneta.shared.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.felixj.moneta.settings.model.CategoryUiModel

@Composable
fun CategoriesGrid(
    categories: List<CategoryUiModel>,
    modifier: Modifier = Modifier,
    trailingCell: (@Composable (Modifier) -> Unit)? = null
) {
    val rows = categories.chunked(2)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEachIndexed { index, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { category ->
                    CategoryCard(category, Modifier.weight(1f))
                }

                if (index == rows.lastIndex && trailingCell != null) {
                    trailingCell(Modifier.weight(1f))
                } else if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        if (trailingCell != null && (rows.isEmpty() || rows.last().size == 2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                trailingCell(Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryUiModel,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(
                        if (category.isExpense) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    )
            ) {
                Icon(
                    painterResource(category.icon),
                    null,
                    modifier = Modifier.padding(8.dp),
                    tint = if (category.isExpense) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                category.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (category.isExpense) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}