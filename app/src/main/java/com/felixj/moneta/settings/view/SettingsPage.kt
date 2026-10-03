package com.felixj.moneta.settings.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import com.felixj.moneta.settings.model.SettingsPageBackupDialog
import com.felixj.moneta.settings.model.SettingsPageUiEvent
import com.felixj.moneta.settings.model.SettingsPageUiState
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.settings.viewmodel.SettingsPageViewModel
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportMode
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

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        viewModel.onUserEvent(SettingsPageUserEvent.ExportFilePicked(uri))
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                is SettingsPageUiEvent.NavigateTo -> {
                    backStack.navigateTo(uiEvent.destination)
                }
                is SettingsPageUiEvent.RequestExportFile -> {
                    exportLauncher.launch(uiEvent.fileName)
                }
                SettingsPageUiEvent.RequestImportFile -> {
                    // ponytail: providers label .csv inconsistently; the parser
                    // still rejects non-backups with a friendly error.
                    importLauncher.launch(
                        arrayOf(
                            "text/csv",
                            "text/comma-separated-values",
                            "application/csv",
                            "text/x-csv",
                            "application/x-csv",
                            "application/vnd.ms-excel"
                        )
                    )
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
fun SettingsPageContent(
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

            buildBackupSection(uiState.backupBusy, onUserEvent)
        }

        BackupDialogs(
            uiState.backupDialog,
            uiState.importModeChoice,
            uiState.currency,
            onUserEvent
        )
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
                CategoriesGrid(
                    categories,
                    onCategoryClick = {
                        onUserEvent(SettingsPageUserEvent.NavigateTo(MonetaRoute.CategoryDetail(it.id)))
                    }
                )

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
                checked = !useSystemTheme && isDarkMode,
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

private fun LazyListScope.buildBackupSection(
    busy: Boolean,
    onUserEvent: (SettingsPageUserEvent) -> Unit
) {
    item {
        Text(
            stringResource(R.string.backup),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }

    item {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onUserEvent(SettingsPageUserEvent.ExportClick) },
                enabled = !busy,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.outline_upload_24), null)
                    Text(stringResource(R.string.export))
                }
            }

            OutlinedButton(
                onClick = { onUserEvent(SettingsPageUserEvent.ImportClick) },
                enabled = !busy,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.outline_download_24), null)
                    Text(stringResource(R.string.import_button))
                }
            }
        }
    }

    if (busy) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun BackupDialogs(
    dialog: SettingsPageBackupDialog,
    importModeChoice: ImportMode,
    currency: Currency,
    onUserEvent: (SettingsPageUserEvent) -> Unit
) {
    when (dialog) {
        SettingsPageBackupDialog.None -> Unit
        is SettingsPageBackupDialog.Preview -> ImportPreviewDialog(dialog, importModeChoice, onUserEvent)
        is SettingsPageBackupDialog.ReplaceConfirm -> ReplaceConfirmDialog(dialog, onUserEvent)
        is SettingsPageBackupDialog.CurrencySwitch -> CurrencySwitchDialog(dialog, currency, onUserEvent)
        is SettingsPageBackupDialog.Message -> BackupMessageDialog(dialog, onUserEvent)
    }
}

@Composable
private fun ImportPreviewDialog(
    dialog: SettingsPageBackupDialog.Preview,
    importModeChoice: ImportMode,
    onUserEvent: (SettingsPageUserEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onUserEvent(SettingsPageUserEvent.DismissBackupDialog) },
        title = { Text(stringResource(R.string.backup_preview_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(
                        R.string.backup_preview,
                        dialog.preview.recordCount,
                        dialog.preview.fileCurrency?.let { stringResource(it.label) }
                            ?: stringResource(R.string.backup_currency_unknown),
                        dialog.preview.categoryCount
                    )
                )
                Text(
                    stringResource(R.string.backup_duplicate_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                ImportModeRow(
                    selected = importModeChoice == ImportMode.KEEP,
                    labelRes = R.string.backup_keep_and_add,
                    tag = "backup_mode_keep",
                    onSelect = { onUserEvent(SettingsPageUserEvent.SelectImportMode(ImportMode.KEEP)) }
                )
                ImportModeRow(
                    selected = importModeChoice == ImportMode.REPLACE,
                    labelRes = R.string.backup_delete_first,
                    tag = "backup_mode_replace",
                    onSelect = { onUserEvent(SettingsPageUserEvent.SelectImportMode(ImportMode.REPLACE)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onUserEvent(SettingsPageUserEvent.ConfirmImportPreview) },
                modifier = Modifier.testTag("backup_preview_confirm")
            ) {
                Text(stringResource(R.string.import_button))
            }
        },
        dismissButton = {
            TextButton({ onUserEvent(SettingsPageUserEvent.DismissBackupDialog) }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ImportModeRow(
    selected: Boolean,
    labelRes: Int,
    tag: String,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable(role = Role.RadioButton, onClick = onSelect)
    ) {
        RadioButton(selected, { onSelect() })
        Text(stringResource(labelRes))
    }
}

@Composable
private fun ReplaceConfirmDialog(
    dialog: SettingsPageBackupDialog.ReplaceConfirm,
    onUserEvent: (SettingsPageUserEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onUserEvent(SettingsPageUserEvent.DismissBackupDialog) },
        title = { Text(stringResource(R.string.backup_replace_title)) },
        text = { Text(stringResource(R.string.backup_replace_confirm, dialog.currentCount)) },
        confirmButton = {
            TextButton(
                onClick = { onUserEvent(SettingsPageUserEvent.ConfirmReplace) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton({ onUserEvent(SettingsPageUserEvent.DismissBackupDialog) }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun CurrencySwitchDialog(
    dialog: SettingsPageBackupDialog.CurrencySwitch,
    currency: Currency,
    onUserEvent: (SettingsPageUserEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onUserEvent(SettingsPageUserEvent.DismissBackupDialog) },
        title = { Text(stringResource(R.string.backup_currency_title)) },
        text = {
            Text(
                stringResource(
                    R.string.backup_currency_switch,
                    dialog.fileCurrencyName,
                    stringResource(currency.label)
                )
            )
        },
        confirmButton = {
            TextButton({ onUserEvent(SettingsPageUserEvent.ConfirmCurrencySwitch) }) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton({ onUserEvent(SettingsPageUserEvent.SkipCurrencySwitch) }) {
                Text(stringResource(R.string.no))
            }
        }
    )
}

@Composable
private fun BackupMessageDialog(
    dialog: SettingsPageBackupDialog.Message,
    onUserEvent: (SettingsPageUserEvent) -> Unit
) {
    val dismiss = { onUserEvent(SettingsPageUserEvent.AcknowledgeBackupMessage) }
    AlertDialog(
        onDismissRequest = dismiss,
        text = { Text(dialog.message.asString()) },
        confirmButton = {
            TextButton(onClick = dismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
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