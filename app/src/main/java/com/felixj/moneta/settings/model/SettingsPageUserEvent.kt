package com.felixj.moneta.settings.model

import android.net.Uri
import com.felixj.moneta.shared.backup.CsvBackupRepository
import com.felixj.moneta.shared.model.MonetaRoute

sealed interface SettingsPageUserEvent {
    data object LoadData: SettingsPageUserEvent
    data class NavigateTo(val destination: MonetaRoute) : SettingsPageUserEvent
    data class ToggleCurrencyDropdown(val isExpanded: Boolean): SettingsPageUserEvent
    data class SelectCurrency(val currency: Currency): SettingsPageUserEvent
    data class ToggleUseSystemTheme(val isOn: Boolean): SettingsPageUserEvent
    data class ToggleDarkMode(val isOn: Boolean): SettingsPageUserEvent
    data object ExportClick : SettingsPageUserEvent
    data class ExportFilePicked(val uri: Uri?) : SettingsPageUserEvent
    data object ImportClick : SettingsPageUserEvent
    data class ImportFilePicked(val uri: Uri?) : SettingsPageUserEvent
    data class SelectImportMode(val mode: CsvBackupRepository.ImportMode) : SettingsPageUserEvent
    data object ConfirmImportPreview : SettingsPageUserEvent
    data object ConfirmReplace : SettingsPageUserEvent
    data object ConfirmCurrencySwitch : SettingsPageUserEvent
    data object SkipCurrencySwitch : SettingsPageUserEvent
    data object DismissBackupDialog : SettingsPageUserEvent
    data object AcknowledgeBackupMessage : SettingsPageUserEvent
}