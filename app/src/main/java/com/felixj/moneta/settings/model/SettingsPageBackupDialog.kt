package com.felixj.moneta.settings.model

import com.felixj.moneta.shared.backup.CsvBackupRepository
import com.felixj.moneta.shared.model.UiText

sealed interface SettingsPageBackupDialog {
    data object None : SettingsPageBackupDialog
    data class Preview(val preview: CsvBackupRepository.ImportPreview) : SettingsPageBackupDialog
    data class ReplaceConfirm(val currentCount: Int) : SettingsPageBackupDialog
    data class CurrencySwitch(val fileCurrencyName: String) : SettingsPageBackupDialog
    data class Message(val message: UiText) : SettingsPageBackupDialog
}
