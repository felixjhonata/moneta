package com.felixj.moneta.settings.viewmodel

import android.net.Uri
import com.felixj.moneta.R
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.settings.model.SettingsPageBackupDialog
import com.felixj.moneta.settings.model.SettingsPageUiEvent
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.shared.backup.CsvBackupRepository
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportMode
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportPreview
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportResult
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.repository.UserPreferencesRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsPageViewModelTest {

    private val categoryRepository = mockk<CategoryRepository>()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val activityRepository = mockk<ActivityRepository>()
    private val backupRepository = mockk<CsvBackupRepository>()
    private val uri = mockk<Uri>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun viewModel() = SettingsPageViewModel(
        categoryRepository,
        userPreferencesRepository,
        activityRepository,
        backupRepository,
        testDispatcher
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadData_populatesCurrencyAndCategories() = runTest {
        every { userPreferencesRepository.isUsingSystemTheme() } returns true
        every { userPreferencesRepository.getCurrency() } returns Currency.IDR
        coEvery { categoryRepository.getCategories(4) } returns emptyList()

        val vm = viewModel()
        vm.onUserEvent(SettingsPageUserEvent.LoadData)

        assertEquals(Currency.IDR, vm.uiState.value.currency)
        assertFalse(vm.uiState.value.showSeeMoreButton)
    }

    @Test
    fun exportClick_emitsRequestWithDatedFilename() = runTest {
        val vm = viewModel()
        val events = mutableListOf<SettingsPageUiEvent>()
        val job = backgroundScope.launch(testDispatcher) { vm.uiEvent.collect { events.add(it) } }

        vm.onUserEvent(SettingsPageUserEvent.ExportClick)

        val request = events.filterIsInstance<SettingsPageUiEvent.RequestExportFile>().single()
        assertTrue(request.fileName.matches(Regex("moneta-backup-\\d{4}-\\d{2}-\\d{2}\\.csv")))
        job.cancel()
    }

    @Test
    fun exportFilePicked_nullUri_doesNothing() = runTest {
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ExportFilePicked(null))

        coVerify(exactly = 0) { backupRepository.exportTo(any()) }
        assertEquals(SettingsPageBackupDialog.None, vm.uiState.value.backupDialog)
    }

    @Test
    fun exportFilePicked_success_showsSavedMessage() = runTest {
        coEvery { backupRepository.exportTo(uri) } returns 3
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ExportFilePicked(uri))

        assertEquals(
            SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_saved, 3)),
            vm.uiState.value.backupDialog
        )
        assertFalse(vm.uiState.value.backupBusy)
    }

    @Test
    fun exportFilePicked_failure_showsError() = runTest {
        coEvery { backupRepository.exportTo(uri) } throws IOException("disk full")
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ExportFilePicked(uri))

        assertEquals(
            SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_export_failed)),
            vm.uiState.value.backupDialog
        )
    }

    @Test
    fun importFilePicked_nullUri_doesNothing() = runTest {
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(null))

        coVerify(exactly = 0) { backupRepository.readImportFile(any()) }
        assertEquals(SettingsPageBackupDialog.None, vm.uiState.value.backupDialog)
    }

    @Test
    fun importFilePicked_unreadable_showsError() = runTest {
        coEvery { backupRepository.readImportFile(uri) } returns null
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))

        assertEquals(
            SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_read_failed)),
            vm.uiState.value.backupDialog
        )
    }

    @Test
    fun importFilePicked_garbage_showsWrongFileError() = runTest {
        coEvery { backupRepository.readImportFile(uri) } returns "not,a,backup"
        every { backupRepository.previewImport("not,a,backup") } returns
            ImportPreview(0, 1, 0, null, false)
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))

        assertEquals(
            SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_wrong_file)),
            vm.uiState.value.backupDialog
        )
        coVerify(exactly = 0) { backupRepository.importCsv(any(), any()) }
    }

    @Test
    fun importFlow_keepWithoutMismatch_importsAndShowsDone() = runTest {
        val text = "csv-content"
        coEvery { backupRepository.readImportFile(uri) } returns text
        every { backupRepository.previewImport(text) } returns ImportPreview(148, 2, 5, Currency.IDR, false)
        coEvery { backupRepository.importCsv(text, ImportMode.KEEP) } returns ImportResult(148, 2, 1)
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
        val preview = vm.uiState.value.backupDialog as SettingsPageBackupDialog.Preview
        assertEquals(148, preview.preview.recordCount)

        vm.onUserEvent(SettingsPageUserEvent.ConfirmImportPreview)

        coVerify(exactly = 1) { backupRepository.importCsv(text, ImportMode.KEEP) }
        assertEquals(
            SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_import_done, 148, 2)),
            vm.uiState.value.backupDialog
        )
    }

    @Test
    fun importFlow_replace_requiresDoubleConfirm() = runTest {
        val text = "csv-content"
        coEvery { backupRepository.readImportFile(uri) } returns text
        every { backupRepository.previewImport(text) } returns ImportPreview(10, 0, 2, Currency.IDR, false)
        coEvery { activityRepository.getActivityCount() } returns 5
        coEvery { backupRepository.importCsv(text, ImportMode.REPLACE) } returns ImportResult(10, 0, 2)
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
        vm.onUserEvent(SettingsPageUserEvent.SelectImportMode(ImportMode.REPLACE))
        vm.onUserEvent(SettingsPageUserEvent.ConfirmImportPreview)

        assertEquals(SettingsPageBackupDialog.ReplaceConfirm(5), vm.uiState.value.backupDialog)
        coVerify(exactly = 0) { backupRepository.importCsv(any(), any()) }

        vm.onUserEvent(SettingsPageUserEvent.ConfirmReplace)

        coVerify(exactly = 1) { backupRepository.importCsv(text, ImportMode.REPLACE) }
        assertEquals(
            SettingsPageBackupDialog.Message(UiText.StringResource(R.string.backup_import_done, 10, 0)),
            vm.uiState.value.backupDialog
        )
    }

    @Test
    fun importFlow_mismatch_confirmSwitchesCurrencyThenImports() = runTest {
        val text = "csv-content"
        coEvery { backupRepository.readImportFile(uri) } returns text
        every { backupRepository.previewImport(text) } returns ImportPreview(10, 0, 2, Currency.IDR, true)
        every { userPreferencesRepository.setCurrency(any()) } just Runs
        coEvery { backupRepository.importCsv(text, ImportMode.KEEP) } returns ImportResult(10, 0, 2)
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
        vm.onUserEvent(SettingsPageUserEvent.ConfirmImportPreview)

        assertEquals(SettingsPageBackupDialog.CurrencySwitch("IDR"), vm.uiState.value.backupDialog)

        vm.onUserEvent(SettingsPageUserEvent.ConfirmCurrencySwitch)

        verify(exactly = 1) { userPreferencesRepository.setCurrency(Currency.IDR) }
        assertEquals(Currency.IDR, vm.uiState.value.currency)
        coVerify(exactly = 1) { backupRepository.importCsv(text, ImportMode.KEEP) }
    }

    @Test
    fun importFlow_mismatch_skipKeepsCurrencyAndImports() = runTest {
        val text = "csv-content"
        coEvery { backupRepository.readImportFile(uri) } returns text
        every { backupRepository.previewImport(text) } returns ImportPreview(10, 0, 2, Currency.IDR, true)
        coEvery { backupRepository.importCsv(text, ImportMode.KEEP) } returns ImportResult(10, 0, 2)
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
        vm.onUserEvent(SettingsPageUserEvent.ConfirmImportPreview)
        vm.onUserEvent(SettingsPageUserEvent.SkipCurrencySwitch)

        verify(exactly = 0) { userPreferencesRepository.setCurrency(any()) }
        coVerify(exactly = 1) { backupRepository.importCsv(text, ImportMode.KEEP) }
    }

    @Test
    fun dismiss_fromPreview_clearsPendingImport() = runTest {
        val text = "csv-content"
        coEvery { backupRepository.readImportFile(uri) } returns text
        every { backupRepository.previewImport(text) } returns ImportPreview(10, 0, 2, Currency.IDR, false)
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
        vm.onUserEvent(SettingsPageUserEvent.DismissBackupDialog)

        assertEquals(SettingsPageBackupDialog.None, vm.uiState.value.backupDialog)

        vm.onUserEvent(SettingsPageUserEvent.ConfirmImportPreview)

        coVerify(exactly = 0) { backupRepository.importCsv(any(), any()) }
    }

    @Test
    fun dismiss_fromReplaceConfirm_stepsBackToPreview() = runTest {
        val text = "csv-content"
        coEvery { backupRepository.readImportFile(uri) } returns text
        every { backupRepository.previewImport(text) } returns ImportPreview(10, 0, 2, Currency.IDR, false)
        coEvery { activityRepository.getActivityCount() } returns 5
        val vm = viewModel()

        vm.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
        vm.onUserEvent(SettingsPageUserEvent.SelectImportMode(ImportMode.REPLACE))
        vm.onUserEvent(SettingsPageUserEvent.ConfirmImportPreview)
        vm.onUserEvent(SettingsPageUserEvent.DismissBackupDialog)

        assertTrue(vm.uiState.value.backupDialog is SettingsPageBackupDialog.Preview)
        coVerify(exactly = 0) { backupRepository.importCsv(any(), any()) }
    }
}
