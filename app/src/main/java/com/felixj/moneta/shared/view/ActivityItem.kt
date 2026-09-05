package com.felixj.moneta.shared.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.felixj.moneta.R
import com.felixj.moneta.shared.model.ActivityItemUiModel


@Composable
fun ActivityItem(
    uiModel: ActivityItemUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (uiModel.isExpense) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(R.drawable.baseline_home_filled_24),
                "home",
                modifier = Modifier.padding(8.dp),
                tint = if (uiModel.isExpense) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                uiModel.activityLabel.asString(),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                uiModel.activityDate.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Text(
            uiModel.amount.asString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (uiModel.isExpense) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
    }
}