package com.felixj.moneta.shared.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.felixj.moneta.shared.room.entity.CategoryType

@Composable
fun CategoryTypeSelector(
    selected: CategoryType,
    onSelect: (CategoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceDim)
    ) {
        Row(Modifier.fillMaxWidth()) {
            CategoryType.entries.forEach { categoryType ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { onSelect(categoryType) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (categoryType == selected) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                ) {
                    Text(
                        categoryType.name,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}