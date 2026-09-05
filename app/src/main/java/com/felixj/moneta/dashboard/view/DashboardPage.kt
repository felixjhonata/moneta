package com.felixj.moneta.dashboard.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.dashboard.model.DashboardPageUiState
import com.felixj.moneta.shared.view.ActivityItem
import com.felixj.moneta.shared.view.BottomNavigationBar
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun DashboardPage(modifier: Modifier = Modifier) {
    DashboardPageContent(
        dummyUiState(), // TODO: Move to viewmodel
        modifier
    )
}

@Composable
private fun DashboardPageContent(
    uiState: DashboardPageUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton({}) {
                Icon(
                    painterResource(R.drawable.baseline_add_24),
                    "add"
                )
            }
        },
        bottomBar = { BottomNavigationBar(BottomNavigationBarDestination.Dashboard) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = innerPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item("app_name") {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            item("total_balance") {
                TotalBalanceCard(
                    uiState.currentBalance,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                )
            }

            item("earned_and_spent") {
                EarnedAndSpentCard(
                    uiState.income,
                    uiState.expense,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                )
            }

            recentActivities(uiState.recentActivities)
        }
    }
}

@Composable
private fun SeeMoreButton(modifier: Modifier = Modifier) {
    Box(modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.see_more),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Icon(
                painterResource(R.drawable.baseline_arrow_forward_24),
                "arrow_forward",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

fun LazyListScope.recentActivities(
    activityItems: List<ActivityItemUiModel>
) {
    item {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.recent_activities))
        }
    }

    if (activityItems.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .height(96.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_activities_yet),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        items(activityItems.take(3)) { item ->
            ActivityItem(
                item,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            )
        }

        if (activityItems.size > 3) {
            item {
                SeeMoreButton(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {}
                        .padding(vertical = 4.dp, horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun MediumCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun EarnedAndSpentCard(
    income: String,
    expense: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MediumCard(
            stringResource(R.string.earned_this_month),
            income,
            modifier = Modifier.weight(1f)
        )

        MediumCard(
            stringResource(R.string.spent_this_month),
            expense,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TotalBalanceCard(
    totalBalance: String,
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.current_balance),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                totalBalance,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun dummyUiState() = DashboardPageUiState(
    stringResource(R.string.rp_value, "4.850.000"),
    stringResource(R.string.rp_value, "1.500.000"),
    stringResource(R.string.rp_value, "800.000"),
    listOf(
        ActivityItemUiModel(
            "Electricity Bills",
            "12 April 2026",
            "-Rp 1.200.000",
            true
        ),
        ActivityItemUiModel(
            "Water Bills",
            "12 April 2026",
            "-Rp 800.000",
            true
        ),
        ActivityItemUiModel(
            "Salary",
            "10 April 2026",
            "+Rp 2.000.000",
            false
        ),
        ActivityItemUiModel(
            "Investment Profit",
            "10 April 2026",
            "+Rp 600.000",
            false
        )
    )
)

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun DashboardPagePreview() {
    MonetaTheme {
        DashboardPageContent(dummyUiState(), Modifier.fillMaxSize())
    }
}

@Preview(
    name = "Dark Mode",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
private fun DashboardPagePreviewDarkMode() {
    MonetaTheme {
        DashboardPageContent(dummyUiState(), Modifier.fillMaxSize())
    }
}