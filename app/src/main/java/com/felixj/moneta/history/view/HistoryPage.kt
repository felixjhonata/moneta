package com.felixj.moneta.history.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felixj.moneta.R
import com.felixj.moneta.history.model.HistoryListItemUiModel
import com.felixj.moneta.history.model.HistoryPageUiState
import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.view.ActivityItem
import com.felixj.moneta.shared.view.BottomNavigationBar
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun HistoryPage(modifier: Modifier = Modifier) {
    HistoryPageContent(
        dummyUiState(),
        modifier
    )
}

private fun dummyUiState() = HistoryPageUiState(
    listOf(
        HistoryListItemUiModel.Date("Today"),
        HistoryListItemUiModel.ActivityItem(
            ActivityItemUiModel(
                UiText.DynamicString("Electricity Bills"),
                UiText.DynamicString("4 September 2026"),
                UiText.DynamicString("-Rp 1.200.000"),
                true,
            )
        ),
        HistoryListItemUiModel.ActivityItem(
            ActivityItemUiModel(
                UiText.DynamicString("Water Bills"),
                UiText.DynamicString("4 September 2026"),
                UiText.DynamicString("-Rp 800.000"),
                true,
            )
        ),
        HistoryListItemUiModel.ActivityItem(
            ActivityItemUiModel(
                UiText.DynamicString("Salary"),
                UiText.DynamicString("4 September 2026"),
                UiText.DynamicString("+Rp 13.000.000"),
                false,
            )
        ),
        HistoryListItemUiModel.Date("Yesterday"),
        HistoryListItemUiModel.ActivityItem(
            ActivityItemUiModel(
                UiText.DynamicString("Phone Bills"),
                UiText.DynamicString("3 September 2026"),
                UiText.DynamicString("-Rp 100.000"),
                true
            )
        )
    )
)

@Composable
private fun HistoryPageContent(
    uiState: HistoryPageUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = { BottomNavigationBar(BottomNavigationBarDestination.History) }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item("page_title") {
                Text(
                    stringResource(R.string.history),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            items(uiState.historyListItems) { item ->
                when (item) {
                    is HistoryListItemUiModel.Date -> {
                        Text(
                            item.date,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    is HistoryListItemUiModel.ActivityItem -> {
                        ActivityItem(
                            item.itemUiModel,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun HistoryPagePreview() {
    MonetaTheme {
        HistoryPageContent(dummyUiState())
    }
}

@Preview(
    "Dark Mode",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
private fun HistoryPagePreviewDarkMode() {
    MonetaTheme {
        HistoryPageContent(dummyUiState())
    }
}