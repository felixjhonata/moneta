package com.felixj.moneta.settings.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.settings.model.SettingsPageBackupDialog
import com.felixj.moneta.settings.model.SettingsPageUiState
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportMode
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportPreview
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.ui.theme.MonetaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupDialogsTest {

    @get:Rule
    val compose = createComposeRule()

    private fun setContent(
        dialog: SettingsPageBackupDialog,
        onUserEvent: (SettingsPageUserEvent) -> Unit = {}
    ) {
        compose.setContent {
            MonetaTheme {
                SettingsPageContent(
                    SettingsPageUiState(backupDialog = dialog),
                    onUserEvent
                )
            }
        }
    }

    @Test
    fun backupSection_showsExportAndImportButtons() {
        val events = mutableListOf<SettingsPageUserEvent>()
        setContent(SettingsPageBackupDialog.None, events::add)

        compose.onNodeWithText("Export").assertIsDisplayed().performClick()
        compose.onNodeWithText("Import").assertIsDisplayed().performClick()

        assertEquals(
            listOf(SettingsPageUserEvent.ExportClick, SettingsPageUserEvent.ImportClick),
            events
        )
    }

    @Test
    fun preview_showsCounts_modesAndConfirms() {
        val events = mutableListOf<SettingsPageUserEvent>()
        setContent(
            SettingsPageBackupDialog.Preview(ImportPreview(120, 2, 5, Currency.IDR, false)),
            events::add
        )

        compose.onNodeWithText("Found 120 records in IDR (Rp), using 5 categories.").assertIsDisplayed()
        compose.onNodeWithText("Importing the same file twice will create copies.").assertIsDisplayed()
        compose.onNodeWithTag("backup_mode_replace").assertIsDisplayed().performClick()
        compose.onNodeWithTag("backup_preview_confirm").performClick()

        assertEquals(
            listOf(
                SettingsPageUserEvent.SelectImportMode(ImportMode.REPLACE),
                SettingsPageUserEvent.ConfirmImportPreview
            ),
            events
        )
    }

    @Test
    fun replaceConfirm_showsCount_deleteConfirms_cancelDismisses() {
        val events = mutableListOf<SettingsPageUserEvent>()
        setContent(SettingsPageBackupDialog.ReplaceConfirm(148), events::add)

        compose.onNodeWithText("This will delete your current 148 records. This cannot be undone.").assertIsDisplayed()
        compose.onNodeWithText("Delete").performClick()

        compose.onNodeWithText("Cancel").performClick()

        assertEquals(
            listOf(
                SettingsPageUserEvent.ConfirmReplace,
                SettingsPageUserEvent.DismissBackupDialog
            ),
            events
        )
    }

    @Test
    fun currencySwitch_yesSwitches_noSkips() {
        val events = mutableListOf<SettingsPageUserEvent>()
        setContent(SettingsPageBackupDialog.CurrencySwitch("IDR"), events::add)

        compose.onNodeWithText("Switch to IDR to show it correctly?", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Yes").performClick()
        compose.onNodeWithText("No").performClick()

        assertEquals(
            listOf(
                SettingsPageUserEvent.ConfirmCurrencySwitch,
                SettingsPageUserEvent.SkipCurrencySwitch
            ),
            events
        )
    }

    @Test
    fun message_okAcknowledges() {
        val events = mutableListOf<SettingsPageUserEvent>()
        setContent(
            SettingsPageBackupDialog.Message(UiText.DynamicString("Done: 148 added, 2 skipped")),
            events::add
        )

        compose.onNodeWithText("Done: 148 added, 2 skipped").assertIsDisplayed()
        compose.onNodeWithText("OK").performClick()

        assertEquals(listOf(SettingsPageUserEvent.AcknowledgeBackupMessage), events)
    }
}
