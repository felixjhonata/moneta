package com.felixj.moneta.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.settings.model.CategoryUiModel
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.settings.model.SettingsPageBackupDialog
import com.felixj.moneta.settings.model.SettingsPageUiEvent
import com.felixj.moneta.settings.model.SettingsPageUiEvent.*
import com.felixj.moneta.settings.model.SettingsPageUiState
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.shared.backup.CsvBackupRepository
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportMode
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportPreview
import com.felixj.moneta.shared.di.IoDispatcher
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_CATEGORIES = 4

@HiltViewModel
class SettingsPageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val activityRepository: ActivityRepository,
    private val backupRepository: CsvBackupRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SettingsPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var pendingImportText: String? = null
    private var pendingPreview: ImportPreview? = null
    private var pendingFileCurrency: Currency? = null

    fun onUserEvent(userEvent: SettingsPageUserEvent) {
        when (userEvent) {
            SettingsPageUserEvent.LoadData -> {
                val usingSystemTheme = userPreferencesRepository.isUsingSystemTheme()
                val isDark =
                    !usingSystemTheme && (userPreferencesRepository.getDarkModePreference() ?: false)
                _uiState.update {
                    it.copy(
                        useSystemTheme = usingSystemTheme,
                        isDarkMode = isDark,
                        currency = userPreferencesRepository.getCurrency()
                    )
                }
                viewModelScope.launch {
                    val categories = categoryRepository.getCategories(MAX_CATEGORIES).map { category ->
                        CategoryUiModel(
                            icon = category.icon,
                            label = category.name,
                            isExpense = !category.name.equals("Salary", ignoreCase = true),
                            id = category.id
                        )
                    }
                    _uiState.update {
                        it.copy(
                            categories = categories,
                            showSeeMoreButton = categories.isNotEmpty()
                        )
                    }
                }
            }
            is SettingsPageUserEvent.NavigateTo -> {
                viewModelScope.launch {
                    _uiEvent.emit(NavigateTo(userEvent.destination))
                }
            }
            is SettingsPageUserEvent.ToggleCurrencyDropdown -> {
                _uiState.update { it.copy(currencyDropdownExpanded = userEvent.isExpanded) }
            }
            is SettingsPageUserEvent.ToggleUseSystemTheme -> {
                userPreferencesRepository.setUseSystemTheme(userEvent.isOn)
                _uiState.update {
                    it.copy(
                        useSystemTheme = userEvent.isOn,
                        isDarkMode = false
                    )
                }
            }
            is SettingsPageUserEvent.ToggleDarkMode -> {
                userPreferencesRepository.setDarkMode(userEvent.isOn)
                _uiState.update { it.copy(isDarkMode = userEvent.isOn) }
            }
            is SettingsPageUserEvent.SelectCurrency -> {
                userPreferencesRepository.setCurrency(userEvent.currency)
                _uiState.update { it.copy(currencyDropdownExpanded = false, currency = userEvent.currency) }
            }
            SettingsPageUserEvent.ExportClick -> {
                viewModelScope.launch(ioDispatcher) {
                    _uiEvent.emit(RequestExportFile(suggestFileName()))
                }
            }
            is SettingsPageUserEvent.ExportFilePicked -> {
                val uri = userEvent.uri ?: return
                viewModelScope.launch(ioDispatcher) {
                    _uiState.update { it.copy(backupBusy = true) }
                    val dialog = try {
                        val count = backupRepository.exportTo(uri)
                        SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_saved, count))
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_export_failed))
                    }
                    _uiState.update { it.copy(backupBusy = false, backupDialog = dialog) }
                }
            }
            SettingsPageUserEvent.ImportClick -> {
                viewModelScope.launch {
                    _uiEvent.emit(RequestImportFile)
                }
            }
            is SettingsPageUserEvent.ImportFilePicked -> {
                val uri = userEvent.uri ?: return
                viewModelScope.launch(ioDispatcher) {
                    _uiState.update { it.copy(backupBusy = true) }
                    val text = backupRepository.readImportFile(uri)
                    if (text == null) {
                        _uiState.update {
                            it.copy(
                                backupBusy = false,
                                backupDialog = SettingsPageBackupDialog.Message(
                                    UiText.StringResource(R.string.backup_read_failed)
                                )
                            )
                        }
                        return@launch
                    }
                    val preview = backupRepository.previewImport(text)
                    if (preview.recordCount == 0) {
                        _uiState.update {
                            it.copy(
                                backupBusy = false,
                                backupDialog = SettingsPageBackupDialog.Message(
                                    UiText.StringResource(R.string.backup_wrong_file)
                                )
                            )
                        }
                        return@launch
                    }
                    pendingImportText = text
                    pendingPreview = preview
                    pendingFileCurrency = preview.fileCurrency
                    _uiState.update {
                        it.copy(
                            backupBusy = false,
                            backupDialog = SettingsPageBackupDialog.Preview(preview),
                            importModeChoice = ImportMode.KEEP
                        )
                    }
                }
            }
            is SettingsPageUserEvent.SelectImportMode -> {
                _uiState.update { it.copy(importModeChoice = userEvent.mode) }
            }
            SettingsPageUserEvent.ConfirmImportPreview -> {
                if (_uiState.value.backupBusy) return
                if (pendingPreview == null) return
                if (_uiState.value.importModeChoice == ImportMode.REPLACE) {
                    viewModelScope.launch(ioDispatcher) {
                        _uiState.update { it.copy(backupBusy = true) }
                        val dialog = try {
                            val count = activityRepository.getActivityCount()
                            SettingsPageBackupDialog.ReplaceConfirm(count)
                        } catch (e: Exception) {
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            clearPendingImport()
                            SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_import_failed))
                        }
                        _uiState.update { it.copy(backupBusy = false, backupDialog = dialog) }
                    }
                } else {
                    proceedAfterMode()
                }
            }
            SettingsPageUserEvent.ConfirmReplace -> {
                if (_uiState.value.backupBusy) return
                proceedAfterMode()
            }
            SettingsPageUserEvent.ConfirmCurrencySwitch -> {
                pendingFileCurrency?.let { fileCurrency ->
                    userPreferencesRepository.setCurrency(fileCurrency)
                    _uiState.update { it.copy(currency = fileCurrency) }
                }
                runImport()
            }
            SettingsPageUserEvent.SkipCurrencySwitch -> runImport()
            SettingsPageUserEvent.DismissBackupDialog -> {
                if (_uiState.value.backupBusy) return
                val preview = pendingPreview
                if (preview == null || _uiState.value.backupDialog is SettingsPageBackupDialog.Preview) {
                    clearPendingImport()
                    _uiState.update { it.copy(backupDialog = SettingsPageBackupDialog.None) }
                } else {
                    _uiState.update { it.copy(backupDialog = SettingsPageBackupDialog.Preview(preview)) }
                }
            }
            SettingsPageUserEvent.AcknowledgeBackupMessage -> {
                _uiState.update { it.copy(backupDialog = SettingsPageBackupDialog.None) }
            }
        }
    }

    private fun proceedAfterMode() {
        val preview = pendingPreview ?: return
        if (preview.currencyMismatch && pendingFileCurrency != null) {
            _uiState.update {
                it.copy(
                    backupDialog = SettingsPageBackupDialog.CurrencySwitch(
                        pendingFileCurrency?.name.orEmpty()
                    )
                )
            }
        } else {
            runImport()
        }
    }

    private fun runImport() {
        val text = pendingImportText ?: return
        val mode = _uiState.value.importModeChoice
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(backupBusy = true, backupDialog = SettingsPageBackupDialog.None) }
            val dialog = try {
                val result = backupRepository.importCsv(text, mode)
                SettingsPageBackupDialog.Message(
                    UiText.StringResource(R.string.backup_import_done, result.added, result.skipped)
                )
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_import_failed))
            }
            clearPendingImport()
            _uiState.update { it.copy(backupBusy = false, backupDialog = dialog) }
        }
    }

    private fun clearPendingImport() {
        pendingImportText = null
        pendingPreview = null
        pendingFileCurrency = null
    }

    private fun suggestFileName(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "moneta-backup-$date.csv"
    }

    companion object {
        fun dummyUiState(isDarkMode: Boolean = false, useSystemTheme: Boolean = true) = SettingsPageUiState(
            categories = listOf(
                CategoryUiModel(
                    R.drawable.baseline_lightbulb_24,
                    "Utilities",
                    true
                ),
                CategoryUiModel(
                    R.drawable.baseline_fastfood_24,
                    "Food",
                    true
                ),
                CategoryUiModel(
                    R.drawable.baseline_directions_bus_24,
                    "Transport",
                    true
                ),
                CategoryUiModel(
                    R.drawable.baseline_account_balance_wallet_24,
                    "Salary",
                    false
                )
            ),
            currency = Currency.IDR,
            useSystemTheme = useSystemTheme,
            isDarkMode = isDarkMode,
            currencies = Currency.entries,
            showSeeMoreButton = true
        )
    }
}
