package com.felixj.moneta.shared.backup

import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.shared.room.converter.CategoryIconConverter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object CsvBackupParser {
    const val HEADER_LINE = "name,amount,currency,datetime,category,category_type,category_icon,notes"
    const val CURRENCY_PREFIX = "# currency="
    const val COLUMN_COUNT = 8

    data class BackupRow(
        val name: String,
        val amount: Long,
        val currency: Currency,
        val datetime: String,
        val category: String,
        val categoryType: String,
        val categoryIcon: String,
        val notes: String
    )

    data class ParsedBackup(
        val currency: Currency?,
        val rows: List<BackupRow>,
        val skipped: Int
    )

    fun export(currency: Currency, rows: List<BackupRow>): String = buildString {
        append(CURRENCY_PREFIX).append(currency.name).append('\n')
        append(HEADER_LINE).append('\n')
        rows.forEach { append(toCsvRow(it)).append('\n') }
    }

    fun toCsvRow(row: BackupRow): String = listOf(
        escape(row.name),
        row.amount.toString(),
        row.currency.name,
        row.datetime,
        escape(row.category),
        row.categoryType,
        normalizeIconCode(row.categoryIcon),
        escape(row.notes)
    ).joinToString(",")

    fun fromCsvRow(line: String): BackupRow? {
        val records = splitRecords(line)
        if (records.size != 1) return null
        val record = records.single()
        if (!record.valid || record.fields.size != COLUMN_COUNT) return null
        return validateRow(record.displayValues())
    }

    fun parse(text: String): ParsedBackup {
        var currency: Currency? = null
        val rows = mutableListOf<BackupRow>()
        var skipped = 0
        var headerConsumed = false
        for (record in splitRecords(text)) {
            if (record.isBlankLine()) continue
            if (!headerConsumed && record.isCurrencyLine()) {
                currency = runCatching {
                    Currency.valueOf(record.fields[0].value.removePrefix(CURRENCY_PREFIX).trim())
                }.getOrNull()
                continue
            }
            if (!headerConsumed) {
                headerConsumed = true
                if (record.valid && record.displayValues().joinToString(",") == HEADER_LINE) continue
            }
            val row = if (record.valid && record.fields.size == COLUMN_COUNT) {
                validateRow(record.displayValues())
            } else {
                null
            }
            if (row == null) skipped++ else rows.add(row)
        }
        return ParsedBackup(currency, rows, skipped)
    }

    fun normalizeIconCode(code: String): String =
        CategoryIconConverter.toCode(CategoryIconConverter.toDrawableRes(code.trim()))

    private fun validateRow(values: List<String>): BackupRow? {
        val name = values[0]
        if (name.isBlank()) return null
        val amount = values[1].toLongOrNull() ?: return null
        if (amount < 0) return null
        val currency = runCatching { Currency.valueOf(values[2]) }.getOrNull() ?: return null
        if (!isValidIsoUtc(values[3])) return null
        val category = values[4]
        if (category.isBlank()) return null
        if (values[5] != "EXPENSE" && values[5] != "INCOME") return null
        return BackupRow(
            name = name,
            amount = amount,
            currency = currency,
            datetime = values[3],
            category = category,
            categoryType = values[5],
            categoryIcon = normalizeIconCode(values[6]),
            notes = values[7]
        )
    }

    private fun escape(field: String): String {
        if (field.none { it == ',' || it == '"' || it == '\n' || it == '\r' } && field == field.trim()) {
            return field
        }
        return "\"" + field.replace("\"", "\"\"") + "\""
    }

    private data class Field(val value: String, val quoted: Boolean)

    private data class Record(val valid: Boolean, val fields: List<Field>) {
        fun displayValues(): List<String> = fields.map { if (it.quoted) it.value else it.value.trim() }

        fun isBlankLine(): Boolean =
            fields.size == 1 && !fields[0].quoted && fields[0].value.isBlank()

        fun isCurrencyLine(): Boolean =
            fields.size == 1 && !fields[0].quoted && fields[0].value.startsWith(CURRENCY_PREFIX)
    }

    private fun splitRecords(text: String): List<Record> {
        val records = mutableListOf<Record>()
        val fields = mutableListOf<Field>()
        val current = StringBuilder()
        var quoted = false
        var inQuotes = false
        var valid = true
        var atFieldStart = true

        fun endField() {
            fields.add(Field(current.toString(), quoted))
            current.clear()
            quoted = false
            atFieldStart = true
        }

        fun endRecord() {
            endField()
            records.add(Record(valid, fields.toList()))
            fields.clear()
            valid = true
        }

        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> when (c) {
                    '"' -> {
                        if (i + 1 < text.length && text[i + 1] == '"') {
                            current.append('"')
                            i++
                        } else {
                            inQuotes = false
                            val next = text.getOrNull(i + 1)
                            if (next != null && next != ',' && next != '\r' && next != '\n') valid = false
                        }
                    }
                    else -> current.append(c)
                }
                c == '"' && atFieldStart -> {
                    inQuotes = true
                    quoted = true
                    atFieldStart = false
                }
                c == ',' -> endField()
                c == '\r' -> {
                    if (text.getOrNull(i + 1) == '\n') i++
                    endRecord()
                }
                c == '\n' -> endRecord()
                else -> {
                    current.append(c)
                    atFieldStart = false
                }
            }
            i++
        }
        if (inQuotes) valid = false
        if (fields.isNotEmpty() || current.isNotEmpty() || quoted || !valid) endRecord()
        return records
    }

    private fun isValidIsoUtc(datetime: String): Boolean {
        return Regex("""^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$""").matches(datetime) && try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }.parse(datetime) != null
        } catch (_: Exception) {
            false
        }
    }
}
