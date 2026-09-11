package com.felixj.moneta.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.felixj.moneta.settings.model.SettingsPageUiEvent
import com.felixj.moneta.settings.model.SettingsPageUiState
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.settings.viewmodel.SettingsPageViewModel
import com.felixj.moneta.shared.util.navigateTo
import com.felixj.moneta.shared.view.BottomNavigationBar
import com.felixj.moneta.shared.view.BottomNavigationBarDestination
import com.felixj.moneta.shared.view.SeeMoreButton
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun SettingsPage(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    viewModel: SettingsPageViewModel = hiltViewModel()
) {
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

            buildCategoriesSection(uiState.categories, uiState.showSeeMoreButton)

            buildPersonalizationSection(
                uiState.isDarkMode,
                uiState.currency,
                uiState.currencies
            )
        }
    }
}

private fun LazyListScope.buildCategoriesSection(
    categories: List<CategoryUiModel>,
    showSeeMoreButton: Boolean
) {
    item {
        Text(
            stringResource(R.string.categories),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }

    item {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.chunked(2).forEach { uiModels ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiModels.forEach { uiModel ->
                        CategoryCard(
                            uiModel.icon,
                            uiModel.label,
                            uiModel.isExpense,
                            Modifier.weight(1f)
                        )
                    }

                    if (uiModels.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            if (showSeeMoreButton) SeeMoreButton({}, Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun CategoryCard(
    iconRes: Int,
    categoryLabel: String,
    isExpense: Boolean,
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
                        if (isExpense) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    )
            ) {
                Icon(
                    painterResource(iconRes),
                    null,
                    modifier = Modifier.padding(8.dp),
                    tint = if (isExpense) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                categoryLabel,
                style = MaterialTheme.typography.titleMedium,
                color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun LazyListScope.buildPersonalizationSection(
    isDarkMode: Boolean,
    currency: String,
    currencies: List<String>
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

            CurrencyDropdown(currency, currencies)
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

            Switch(isDarkMode, {})
        }
    }
}

@Composable
private fun CurrencyDropdown(
    currency: String,
    currencies: List<String>,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        OutlinedCard {
            Row(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    currency,
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    painterResource(R.drawable.baseline_arrow_drop_down_24),
                    "arrow_drop_down"
                )
            }
        }

        DropdownMenu(
            false,
            {}
        ) {
            currencies.forEach {
                DropdownMenuItem({ Text(it) }, {})
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