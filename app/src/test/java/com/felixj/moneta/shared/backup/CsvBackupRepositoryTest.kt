package com.felixj.moneta.shared.backup

import androidx.room3.TransactionScope
import androidx.room3.withWriteTransaction
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.felixj.moneta.R
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportMode
import com.felixj.moneta.shared.backup.CsvBackupRepository.ImportResult
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.repository.UserPreferencesRepository
import com.felixj.moneta.shared.room.db.MonetaDatabase
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import com.felixj.moneta.shared.room.entity.Category
import com.felixj.moneta.shared.room.entity.CategoryType
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class CsvBackupRepositoryTest {

    private val context = mockk<Context>()
    private val resolver = mockk<ContentResolver>()
    private val uri = mockk<Uri>()
    private val database = mockk<MonetaDatabase>()
    private val activityRepository = mockk<ActivityRepository>()
    private val categoryRepository = mockk<CategoryRepository>()
    private val prefs = mockk<UserPreferencesRepository>()
    private val repository = CsvBackupRepository(context, database, activityRepository, categoryRepository, prefs)

    @Before
    fun setUp() {
        every { context.contentResolver } returns resolver
        every { uri.scheme } returns "content"
        mockkStatic("androidx.room3.RoomDatabaseKt")
        coEvery { database.withWriteTransaction<ImportResult>(any()) } coAnswers {
            secondArg<suspend TransactionScope<ImportResult>.() -> ImportResult>().invoke(mockk())
        }
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room3.RoomDatabaseKt")
    }

    @Test
    fun exportCsv_joinsActivitiesWithCategories() = runTest {
        every { prefs.getCurrency() } returns Currency.IDR
        coEvery { categoryRepository.getCategories() } returns listOf(
            Category(1, "Food", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE),
            Category(2, "Salary", R.drawable.baseline_account_balance_wallet_24, CategoryType.INCOME)
        )
        coEvery { activityRepository.getActivities() } returns listOf(
            ActivityWithCategoryIcon(
                Activity(1, 1, "Lunch", "2026-09-01T12:30:00Z", 50000, "with friends"),
                R.drawable.baseline_fastfood_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(2, 2, "Salary", "2026-09-01T09:00:00Z", 5000000, "September"),
                R.drawable.baseline_account_balance_wallet_24,
                CategoryType.INCOME
            )
        )

        val csv = repository.exportCsv()

        val parsed = CsvBackupParser.parse(csv)
        assertEquals(Currency.IDR, parsed.currency)
        assertEquals(0, parsed.skipped)
        assertEquals(2, parsed.rows.size)
        assertEquals("Lunch", parsed.rows[0].name)
        assertEquals(50000L, parsed.rows[0].amount)
        assertEquals("2026-09-01T12:30:00Z", parsed.rows[0].datetime)
        assertEquals("baseline_fastfood_24", parsed.rows[0].categoryIcon)
    }

    @Test
    fun importCsv_keepMode_reusesExistingCategoryAndCreatesNew() = runTest {
        val text = CsvBackupParser.export(
            Currency.IDR,
            listOf(
                backupRow("Lunch", 50000, "Food", "EXPENSE", "baseline_fastfood_24"),
                backupRow("Bus", 15000, "Transport", "EXPENSE", "baseline_directions_bus_24")
            )
        )
        coEvery { categoryRepository.getCategories() } returns listOf(
            Category(1, "Food", R.drawable.baseline_lightbulb_24, CategoryType.EXPENSE)
        )
        coEvery { activityRepository.getNextActivityId() } returns 10
        coEvery { categoryRepository.getNextCategoryId() } returns 5
        val insertedCategories = mutableListOf<List<Category>>()
        val insertedActivities = mutableListOf<List<Activity>>()
        coEvery { categoryRepository.insertCategories(capture(insertedCategories)) } just Runs
        coEvery { activityRepository.insertActivities(capture(insertedActivities)) } just Runs

        val result = repository.importCsv(text, ImportMode.KEEP)

        assertEquals(ImportResult(added = 2, skipped = 0, categoriesCreated = 1), result)
        assertEquals(1, insertedCategories.single().size)
        assertEquals(Category(5, "Transport", R.drawable.baseline_directions_bus_24, CategoryType.EXPENSE), insertedCategories.single()[0])
        assertEquals(2, insertedActivities.single().size)
        assertEquals(Activity(11, 1, "Lunch", "2026-09-01T12:30:00Z", 50000, ""), insertedActivities.single()[0])
        assertEquals(Activity(10, 5, "Bus", "2026-09-01T12:30:00Z", 15000, ""), insertedActivities.single()[1])
        coVerify(exactly = 0) { activityRepository.deleteAllActivities() }
        coVerify(exactly = 0) { categoryRepository.deleteAllCategories() }
    }

    @Test
    fun importCsv_replaceMode_deletesFirstThenInserts() = runTest {
        val text = CsvBackupParser.export(Currency.IDR, listOf(backupRow("Lunch", 50000, "Food", "EXPENSE", "baseline_fastfood_24")))
        coEvery { activityRepository.deleteAllActivities() } just Runs
        coEvery { categoryRepository.deleteAllCategories() } just Runs
        coEvery { activityRepository.getNextActivityId() } returns 1
        coEvery { categoryRepository.getNextCategoryId() } returns 1
        coEvery { categoryRepository.insertCategories(any()) } just Runs
        coEvery { activityRepository.insertActivities(any()) } just Runs

        val result = repository.importCsv(text, ImportMode.REPLACE)

        assertEquals(ImportResult(added = 1, skipped = 0, categoriesCreated = 1), result)
        coVerifyOrder {
            activityRepository.deleteAllActivities()
            categoryRepository.deleteAllCategories()
            categoryRepository.insertCategories(any())
            activityRepository.insertActivities(any())
        }
        coVerify(exactly = 0) { categoryRepository.getCategories() }
    }

    @Test
    fun importCsv_emptyFile_neverDeletesData() = runTest {
        val result = repository.importCsv("# currency=IDR\n" + CsvBackupParser.HEADER_LINE + "\n", ImportMode.REPLACE)

        assertEquals(ImportResult(added = 0, skipped = 0, categoriesCreated = 0), result)
        coVerify(exactly = 0) { database.withWriteTransaction<ImportResult>(any()) }
        coVerify(exactly = 0) { activityRepository.deleteAllActivities() }
        coVerify(exactly = 0) { categoryRepository.deleteAllCategories() }
    }

    @Test
    fun planImport_matchesNameCaseInsensitive_keepsExistingIcon() {
        val planned = repository.planImport(
            rows = listOf(backupRow("Lunch", 50000, "food", "EXPENSE", "baseline_fastfood_24")),
            existingCategories = listOf(Category(3, "FOOD", R.drawable.baseline_lightbulb_24, CategoryType.EXPENSE)),
            nextActivityId = 7,
            nextCategoryId = 9
        )

        assertTrue(planned.categoriesToInsert.isEmpty())
        assertEquals(1, planned.activitiesToInsert.size)
        assertEquals(3, planned.activitiesToInsert[0].categoryId)
        assertEquals(7, planned.activitiesToInsert[0].id)
    }

    @Test
    fun planImport_sameNameDifferentType_createsSeparateCategory() {
        val planned = repository.planImport(
            rows = listOf(backupRow("TopUp", 100000, "Food", "INCOME", "baseline_fastfood_24")),
            existingCategories = listOf(Category(1, "Food", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE)),
            nextActivityId = 1,
            nextCategoryId = 2
        )

        assertEquals(1, planned.categoriesToInsert.size)
        assertEquals(CategoryType.INCOME, planned.categoriesToInsert[0].type)
        assertEquals(2, planned.activitiesToInsert[0].categoryId)
    }

    @Test
    fun planImport_sameDatetime_earlierRowsGetHigherIds_preservesDisplayOrder() {
        val planned = repository.planImport(
            rows = listOf(
                backupRow("Newer", 50000, "Food", "EXPENSE", "baseline_fastfood_24"),
                backupRow("Older", 40000, "Food", "EXPENSE", "baseline_fastfood_24")
            ),
            existingCategories = emptyList(),
            nextActivityId = 10,
            nextCategoryId = 5
        )

        assertEquals(11, planned.activitiesToInsert[0].id)
        assertEquals(10, planned.activitiesToInsert[1].id)
    }

    @Test
    fun previewImport_reportsCountsAndMismatch() {
        every { prefs.getCurrency() } returns Currency.USD
        val text = CsvBackupParser.export(
            Currency.IDR,
            listOf(
                backupRow("Lunch", 50000, "Food", "EXPENSE", "baseline_fastfood_24"),
                backupRow("Dinner", 40000, "Food", "EXPENSE", "baseline_fastfood_24")
            )
        )

        val preview = repository.previewImport(text)

        assertEquals(2, preview.recordCount)
        assertEquals(0, preview.skipped)
        assertEquals(1, preview.categoryCount)
        assertEquals(Currency.IDR, preview.fileCurrency)
        assertTrue(preview.currencyMismatch)
    }

    @Test
    fun previewImport_sameCurrency_noMismatch() {
        every { prefs.getCurrency() } returns Currency.IDR
        val text = CsvBackupParser.export(Currency.IDR, listOf(backupRow("Lunch", 50000, "Food", "EXPENSE", "baseline_fastfood_24")))

        val preview = repository.previewImport(text)

        assertFalse(preview.currencyMismatch)
    }

    @Test
    fun exportTo_writesCsvAndReturnsCount() = runTest {
        every { prefs.getCurrency() } returns Currency.IDR
        coEvery { categoryRepository.getCategories() } returns listOf(
            Category(1, "Food", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE)
        )
        coEvery { activityRepository.getActivities() } returns listOf(
            ActivityWithCategoryIcon(
                Activity(1, 1, "Lunch", "2026-09-01T12:30:00Z", 50000, ""),
                R.drawable.baseline_fastfood_24,
                CategoryType.EXPENSE
            )
        )
        val out = ByteArrayOutputStream()
        every { resolver.openOutputStream(uri) } returns out

        val count = repository.exportTo(uri)

        assertEquals(1, count)
        assertTrue(out.toString().contains("Lunch,50000,IDR,2026-09-01T12:30:00Z,Food,EXPENSE,baseline_fastfood_24,"))
    }

    @Test
    fun exportTo_throwsWhenFileCannotBeOpened() = runTest {
        every { prefs.getCurrency() } returns Currency.IDR
        coEvery { categoryRepository.getCategories() } returns emptyList()
        coEvery { activityRepository.getActivities() } returns emptyList()
        every { resolver.openOutputStream(uri) } returns null

        try {
            repository.exportTo(uri)
            fail("expected IOException")
        } catch (_: IOException) {
        }
    }

    @Test
    fun readImportFile_returnsFileContent() = runTest {
        val text = "# currency=IDR\n" + CsvBackupParser.HEADER_LINE + "\n"
        every { resolver.openInputStream(uri) } returns ByteArrayInputStream(text.toByteArray())

        assertEquals(text, repository.readImportFile(uri))
    }

    @Test
    fun readImportFile_returnsNullWhenUnreadable() = runTest {
        every { resolver.openInputStream(uri) } throws SecurityException("denied")

        assertNull(repository.readImportFile(uri))
    }

    private fun backupRow(
        name: String,
        amount: Long,
        category: String,
        type: String,
        icon: String
    ) = CsvBackupParser.BackupRow(
        name = name,
        amount = amount,
        currency = Currency.IDR,
        datetime = "2026-09-01T12:30:00Z",
        category = category,
        categoryType = type,
        categoryIcon = icon,
        notes = ""
    )
}
