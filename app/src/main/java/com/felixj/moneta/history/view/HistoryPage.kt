package com.felixj.moneta.history.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.felixj.moneta.R
import com.felixj.moneta.history.model.HistoryListItemUiModel
import com.felixj.moneta.history.model.HistoryPageUiEvent
import com.felixj.moneta.history.model.HistoryPageUserEvent
import com.felixj.moneta.history.viewmodel.HistoryPageViewModel
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.util.navigateTo
import com.felixj.moneta.shared.view.ActivityItem
import com.felixj.moneta.shared.view.BottomNavigationBar
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import com.felixj.moneta.shared.view.rememberResponsiveFontSizeGroup
import com.felixj.moneta.ui.theme.MonetaTheme
import kotlinx.coroutines.flow.flowOf

@Composable
fun HistoryPage(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    viewModel: HistoryPageViewModel = hiltViewModel()
) {
    val pagedItems = viewModel.pagedHistory.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                is HistoryPageUiEvent.NavigateTo -> {
                    backStack.navigateTo(uiEvent.destination)
                }
            }
        }
    }

    HistoryPageContent(
        pagedItems,
        viewModel::onUserEvent,
        modifier
    )
}

@Composable
private fun HistoryPageContent(
    pagedItems: LazyPagingItems<HistoryListItemUiModel>,
    onUserEvent: (HistoryPageUserEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                { onUserEvent(HistoryPageUserEvent.NavigateTo(MonetaRoute.AddActivity)) }
            ) {
                Icon(
                    painterResource(R.drawable.baseline_add_24),
                    "add"
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                BottomNavigationBarDestination.History,
                { onUserEvent(HistoryPageUserEvent.NavigateTo(it.destination)) }
            )
        }
    ) { innerPadding ->
        val activityGroup =
            rememberResponsiveFontSizeGroup(MaterialTheme.typography.titleMedium.fontSize)

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

            when (pagedItems.loadState.refresh) {
                is LoadState.Loading -> {
                    item("refresh_loading") {
                        Box(
                            modifier = Modifier.height(96.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item("refresh_error") {
                        Box(
                            modifier = Modifier.height(96.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { pagedItems.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is LoadState.NotLoading -> {
                    if (pagedItems.itemCount == 0) {
                        item("empty") {
                            Box(
                                modifier = Modifier.height(96.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.no_activities_yet),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            items(
                count = pagedItems.itemCount,
                key = { index ->
                    when (val item = pagedItems[index]) {
                        is HistoryListItemUiModel.Date -> "header-${item.date}"
                        is HistoryListItemUiModel.ActivityItem -> "activity-${item.itemUiModel.activityId}"
                        null -> "placeholder-$index"
                    }
                }
            ) { index ->
                when (val item = pagedItems[index]) {
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
                            onClick = {
                                onUserEvent(
                                    HistoryPageUserEvent.ActivityItemClick(item.itemUiModel.activityId)
                                )
                            },
                            modifier = Modifier.padding(horizontal = 24.dp),
                            sizeGroup = activityGroup
                        )
                    }

                    null -> Unit
                }
            }

            when (pagedItems.loadState.append) {
                is LoadState.Loading -> {
                    item("append_loading") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item("append_error") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { pagedItems.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> Unit
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
        HistoryPageContent(
            flowOf(PagingData.from(HistoryPageViewModel.dummyItems())).collectAsLazyPagingItems(),
            {}
        )
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
        HistoryPageContent(
            flowOf(PagingData.from(HistoryPageViewModel.dummyItems())).collectAsLazyPagingItems(),
            {}
        )
    }
}
