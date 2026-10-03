package com.felixj.moneta.shared.backup

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.felixj.moneta.R
import com.felixj.moneta.settings.model.SettingsPageBackupDialog
import com.felixj.moneta.settings.model.SettingsPageUserEvent
import com.felixj.moneta.settings.viewmodel.SettingsPageViewModel
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportMode
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.repository.UserPreferencesRepository
import com.felixj.moneta.shared.room.db.MonetaDatabase
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.Category
import com.felixj.moneta.shared.room.entity.CategoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BackupImportE2ETest {

    private lateinit var context: Context
    private lateinit var database: MonetaDatabase
    private lateinit var activityRepository: ActivityRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var backupRepository: CsvBackupRepository
    private lateinit var viewModel: SettingsPageViewModel

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("e2e-backup.db")
        database = Room.databaseBuilder<MonetaDatabase>(context, "e2e-backup.db")
            .setDriver(AndroidSQLiteDriver())
            .build()
        activityRepository = ActivityRepository(database.activityDao())
        categoryRepository = CategoryRepository(database.categoryDao())
        val prefs = UserPreferencesRepository(context)
        backupRepository = CsvBackupRepository(context, database, activityRepository, categoryRepository, prefs)
        viewModel = SettingsPageViewModel(
            categoryRepository, prefs, activityRepository, backupRepository, Dispatchers.Unconfined
        )
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase("e2e-backup.db")
    }

    private fun awaitDialog(
        timeoutMs: Long = 10_000,
        condition: (SettingsPageBackupDialog) -> Boolean
    ): SettingsPageBackupDialog {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (true) {
            val dialog = viewModel.uiState.value.backupDialog
            if (condition(dialog)) return dialog
            if (SystemClock.uptimeMillis() > deadline) fail("timed out waiting for backup dialog")
            Thread.sleep(50)
        }
    }

    private fun seedLunch() = runBlocking {
        categoryRepository.insertCategory(
            Category(1, "Food", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE)
        )
        activityRepository.insertActivity(
            Activity(1, 1, "Lunch", "2026-09-01T12:30:00Z", 50000, "")
        )
    }

    private fun exportToCache(name: String): Uri = runBlocking {
        val file = File(context.cacheDir, name)
        if (file.exists()) file.delete()
        val uri = Uri.fromFile(file)
        assertEquals(1, backupRepository.exportTo(uri))
        assertTrue(file.length() > 0)
        uri
    }

    @Test
    fun importKeep_addsOnTop_reusesExistingCategory() {
        seedLunch()
        val uri = exportToCache("e2e-keep.csv")

        viewModel.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
        val preview = awaitDialog(condition = { it is SettingsPageBackupDialog.Preview })
            as SettingsPageBackupDialog.Preview
        assertEquals(1, preview.preview.recordCount)

        viewModel.onUserEvent(SettingsPageUserEvent.ConfirmImportPreview)
        awaitDialog(condition = { it is SettingsPageBackupDialog.Message })

        runBlocking {
            assertEquals(2, activityRepository.getActivities().size)
            assertEquals(1, categoryRepository.getCategories().size)
        }
    }

    @Test
    fun importReplace_wipesFirst_thenRestores() {
        seedLunch()
        val uri = exportToCache("e2e-replace.csv")

        viewModel.onUserEvent(SettingsPageUserEvent.ImportFilePicked(uri))
        viewModel.onUserEvent(SettingsPageUserEvent.SelectImportMode(ImportMode.REPLACE))
        viewModel.onUserEvent(SettingsPageUserEvent.ConfirmImportPreview)
        awaitDialog(condition = { it is SettingsPageBackupDialog.ReplaceConfirm })

        viewModel.onUserEvent(SettingsPageUserEvent.ConfirmReplace)
        awaitDialog(condition = { it is SettingsPageBackupDialog.Message })

        runBlocking {
            val activities = activityRepository.getActivities()
            assertEquals(1, activities.size)
            assertEquals("Lunch", activities[0].activity.name)
            assertEquals(50000L, activities[0].activity.amount)
        }
    }

    @Test
    fun importWrongFile_showsError_keepsData() {
        seedLunch()
        val file = File(context.cacheDir, "e2e-garbage.csv")
        file.writeText("this,is,not,a,moneta,backup\n1,2,3\n")
        viewModel.onUserEvent(SettingsPageUserEvent.ImportFilePicked(Uri.fromFile(file)))

        awaitDialog(condition = { it is SettingsPageBackupDialog.Message })
        runBlocking {
            assertEquals(1, activityRepository.getActivities().size)
        }
    }
}
