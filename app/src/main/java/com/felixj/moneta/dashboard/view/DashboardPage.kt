package com.felixj.moneta.dashboard.view

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
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felixj.moneta.R
import com.felixj.moneta.dashboard.model.DashboardPageUiState
import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.view.ActivityItem
import com.felixj.moneta.shared.view.BottomNavigationBar
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import com.felixj.moneta.shared.view.SeeMoreButton
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun DashboardPage(modifier: Modifier = Modifier) {
    DashboardPageContent(
        dummyUiState(),
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
                    uiState.currentBalance.asString(),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                )
            }

            item("earned_and_spent") {
                EarnedAndSpentCard(
                    uiState.income.asString(),
                    uiState.expense.asString(),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                )
            }

            recentActivities(uiState.recentActivities, uiState.showSeeMoreButton)
        }
    }
}

fun LazyListScope.recentActivities(
    activityItems: List<ActivityItemUiModel>,
    showSeeMoreButton: Boolean
) {
    item {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.recent_activities),
                style = MaterialTheme.typography.titleMedium
            )
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
        items(activityItems) { item ->
            ActivityItem(
                item,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            )
        }

        if (showSeeMoreButton) {
            item {
                SeeMoreButton()
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

private fun dummyUiState() = DashboardPageUiState(
    UiText.StringResource(R.string.rp_value, "4.850.000"),
    UiText.StringResource(R.string.rp_value, "1.500.000"),
    UiText.StringResource(R.string.rp_value, "800.000"),
    listOf(
        ActivityItemUiModel(
            UiText.DynamicString("Electricity Bills"),
            UiText.DynamicString("12 April 2026"),
            UiText.DynamicString("-Rp 1.200.000"),
            true
        ),
        ActivityItemUiModel(
            UiText.DynamicString("Water Bills"),
            UiText.DynamicString("12 April 2026"),
            UiText.DynamicString("-Rp 800.000"),
            true
        ),
        ActivityItemUiModel(
            UiText.DynamicString("Salary"),
            UiText.DynamicString("10 April 2026"),
            UiText.DynamicString("+Rp 2.000.000"),
            false
        )
    ),
    true
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