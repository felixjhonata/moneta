package com.felixj.moneta.shared.backup

import androidx.room3.withWriteTransaction
import android.content.Context
import android.net.Uri
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.repository.UserPreferencesRepository
import com.felixj.moneta.shared.room.converter.CategoryIconConverter
import com.felixj.moneta.shared.room.db.MonetaDatabase
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.Category
import com.felixj.moneta.shared.room.entity.CategoryType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MonetaDatabase,
    private val activityRepository: ActivityRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    enum class ImportMode { KEEP, REPLACE }

    data class ImportPreview(
        val recordCount: Int,
        val skipped: Int,
        val categoryCount: Int,
        val fileCurrency: Currency?,
        val currencyMismatch: Boolean
    )

    data class ImportResult(val added: Int, val skipped: Int, val categoriesCreated: Int)

    data class PlannedImport(
        val categoriesToInsert: List<Category>,
        val activitiesToInsert: List<Activity>
    )

    suspend fun exportCsv(): String {
        val (currency, rows) = collectExportRows()
        return CsvBackupParser.export(currency, rows)
    }

    suspend fun exportTo(uri: Uri): Int {
        val (currency, rows) = collectExportRows()
        val csv = CsvBackupParser.export(currency, rows)
        openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
            ?: throw IOException("Cannot open file for writing")
        return rows.size
    }

    fun readImportFile(uri: Uri): String? = try {
        openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
    } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) throw e
        null
    }

    private suspend fun collectExportRows(): Pair<Currency, List<CsvBackupParser.BackupRow>> {
        val currency = userPreferencesRepository.getCurrency()
        val categoriesById = categoryRepository.getCategories().associateBy { it.id }
        val rows = activityRepository.getActivities().mapNotNull { item ->
            val category = categoriesById[item.activity.categoryId] ?: return@mapNotNull null
            CsvBackupParser.BackupRow(
                name = item.activity.name,
                amount = item.activity.amount,
                currency = currency,
                datetime = item.activity.date,
                category = category.name,
                categoryType = category.type.name,
                categoryIcon = CategoryIconConverter.toCode(category.icon),
                notes = item.activity.notes
            )
        }
        return currency to rows
    }

    private fun openInputStream(uri: Uri): java.io.InputStream? =
        if (uri.scheme == "file") uri.path?.let { java.io.FileInputStream(it) }
        else context.contentResolver.openInputStream(uri)

    private fun openOutputStream(uri: Uri): java.io.OutputStream? =
        if (uri.scheme == "file") uri.path?.let { java.io.FileOutputStream(it) }
        else context.contentResolver.openOutputStream(uri)

    fun previewImport(text: String): ImportPreview {
        val parsed = CsvBackupParser.parse(text)
        val fileCurrency = parsed.currency ?: parsed.rows.firstOrNull()?.currency
        return ImportPreview(
            recordCount = parsed.rows.size,
            skipped = parsed.skipped,
            categoryCount = parsed.rows.map { categoryKey(it.category, it.categoryType) }.toSet().size,
            fileCurrency = fileCurrency,
            currencyMismatch = fileCurrency != null && fileCurrency != userPreferencesRepository.getCurrency()
        )
    }

    suspend fun importCsv(text: String, mode: ImportMode): ImportResult {
        val parsed = CsvBackupParser.parse(text)
        if (parsed.rows.isEmpty()) return ImportResult(added = 0, skipped = parsed.skipped, categoriesCreated = 0)
        return database.withWriteTransaction {
            if (mode == ImportMode.REPLACE) {
                activityRepository.deleteAllActivities()
                categoryRepository.deleteAllCategories()
            }
            val existing = if (mode == ImportMode.REPLACE) emptyList() else categoryRepository.getCategories()
            val planned = planImport(
                rows = parsed.rows,
                existingCategories = existing,
                nextActivityId = activityRepository.getNextActivityId(),
                nextCategoryId = categoryRepository.getNextCategoryId()
            )
            if (planned.categoriesToInsert.isNotEmpty()) {
                categoryRepository.insertCategories(planned.categoriesToInsert)
            }
            if (planned.activitiesToInsert.isNotEmpty()) {
                activityRepository.insertActivities(planned.activitiesToInsert)
            }
            ImportResult(
                added = planned.activitiesToInsert.size,
                skipped = parsed.skipped,
                categoriesCreated = planned.categoriesToInsert.size
            )
        }
    }

    fun planImport(
        rows: List<CsvBackupParser.BackupRow>,
        existingCategories: List<Category>,
        nextActivityId: Int,
        nextCategoryId: Int
    ): PlannedImport {
        val resolved = existingCategories.associateBy { categoryKey(it.name, it.type.name) }.toMutableMap()
        val categoriesToInsert = mutableListOf<Category>()
        var newCategoryId = nextCategoryId
        for ((_, _, _, _, category, categoryType, categoryIcon) in rows) {
            val key = categoryKey(category, categoryType)
            if (key !in resolved) {
                val created = Category(
                    id = newCategoryId++,
                    name = category.trim(),
                    icon = CategoryIconConverter.toDrawableRes(CsvBackupParser.normalizeIconCode(
                        categoryIcon
                    )),
                    type = CategoryType.valueOf(categoryType)
                )
                resolved[key] = created
                categoriesToInsert.add(created)
            }
        }
        // ponytail: export is newest-first, so the last row gets the smallest id;
        // same-datetime rows then keep their order under ORDER BY date DESC, id DESC.
        val activitiesToInsert = rows.mapIndexed { index, row ->
            Activity(
                id = nextActivityId + (rows.size - 1 - index),
                categoryId = resolved.getValue(categoryKey(row.category, row.categoryType)).id,
                name = row.name,
                date = row.datetime,
                amount = row.amount,
                notes = row.notes
            )
        }
        return PlannedImport(categoriesToInsert, activitiesToInsert)
    }

    private fun categoryKey(name: String, type: String): String =
        name.trim().lowercase() + "|" + type.trim().uppercase()
}
