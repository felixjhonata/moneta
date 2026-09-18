package com.felixj.moneta.settings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.felixj.moneta.R
import com.felixj.moneta.settings.model.CategoryUiModel
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.settings.model.SettingsPageUiEvent
import com.felixj.moneta.settings.model.SettingsPageUiState
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.settings.viewmodel.SettingsPageViewModel
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.util.navigateTo
import com.felixj.moneta.shared.view.BottomNavigationBar
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import com.felixj.moneta.shared.view.CategoriesGrid
import com.felixj.moneta.shared.view.SeeMoreButton
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun SettingsPage(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    viewModel: SettingsPageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onUserEvent(SettingsPageUserEvent.LoadData)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                is SettingsPageUiEvent.NavigateTo -> {
                    backStack.navigateTo(uiEvent.destination)
                }
            }
        }
    }

    SettingsPageContent(
        uiState,
        viewModel::onUserEvent,
        modifier
    )
}

@Composable
private fun SettingsPageContent(
    uiState: SettingsPageUiState,
    onUserEvent: (SettingsPageUserEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigationBar(
                BottomNavigationBarDestination.Settings,
                { onUserEvent(SettingsPageUserEvent.NavigateTo(it.destination)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item("page_title") {
                Text(
                    stringResource(R.string.settings),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            buildCategoriesSection(uiState.categories, uiState.showSeeMoreButton, onUserEvent)

            buildPersonalizationSection(
                uiState.useSystemTheme,
                uiState.isDarkMode,
                uiState.currency,
                uiState.currencies,
                uiState.currencyDropdownExpanded,
                onUserEvent
            )
        }
    }
}

private fun LazyListScope.buildCategoriesSection(
    categories: List<CategoryUiModel>,
    showSeeMoreButton: Boolean,
    onUserEvent: (SettingsPageUserEvent) -> Unit
) {
    item {
        Text(
            stringResource(R.string.categories),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }

    if (categories.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .height(96.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        stringResource(R.string.no_categories),
                        color = MaterialTheme.colorScheme.outline
                    )

                    TextButton({}) {
                        Text(stringResource(R.string.add_category))
                    }
                }
            }
        }
    } else {
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoriesGrid(categories)

                if (showSeeMoreButton) SeeMoreButton(
                    { onUserEvent(SettingsPageUserEvent.NavigateTo(MonetaRoute.Categories)) },
                    Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

private fun LazyListScope.buildPersonalizationSection(
    useSystemTheme: Boolean,
    isDarkMode: Boolean,
    currency: Currency,
    currencies: List<Currency>,
    currencyDropdownExpanded: Boolean,
    onUserEvent: (SettingsPageUserEvent) -> Unit
) {
    item {
        Text(
            stringResource(R.string.personalization),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }

    item {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.currency),
                modifier = Modifier.weight(1f)
            )

            CurrencyDropdown(currency, currencies, currencyDropdownExpanded, onUserEvent)
        }
    }

    item {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.use_system_theme),
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = useSystemTheme,
                onCheckedChange = { onUserEvent(SettingsPageUserEvent.ToggleUseSystemTheme(it)) }
            )
        }
    }

    item {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.dark_mode),
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = if (useSystemTheme) false else isDarkMode,
                enabled = !useSystemTheme,
                onCheckedChange = { onUserEvent(SettingsPageUserEvent.ToggleDarkMode(it)) }
            )
        }
    }
}

@Composable
private fun CurrencyDropdown(
    currency: Currency,
    currencies: List<Currency>,
    currencyDropdownExpanded: Boolean,
    onUserEvent: (SettingsPageUserEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        OutlinedCard(
            onClick = { onUserEvent(SettingsPageUserEvent.ToggleCurrencyDropdown(true)) }
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(currency.label),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    painterResource(R.drawable.baseline_arrow_drop_down_24),
                    "arrow_drop_down"
                )
            }
        }

        DropdownMenu(
            currencyDropdownExpanded,
            { onUserEvent(SettingsPageUserEvent.ToggleCurrencyDropdown(false)) }
        ) {
            currencies.forEach {
                DropdownMenuItem(
                    { Text(stringResource(it.label)) },
                    { onUserEvent(SettingsPageUserEvent.SelectCurrency(it)) }
                )
            }
        }
    }
}

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun SettingsPageContentPreview() {
    MonetaTheme {
        SettingsPageContent(SettingsPageViewModel.dummyUiState(), {})
    }
}

@Preview(
    name = "Regular",
    showSystemUi = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsPageContentDarkModePreview() {
    MonetaTheme {
        SettingsPageContent(SettingsPageViewModel.dummyUiState(true), {})
    }
}