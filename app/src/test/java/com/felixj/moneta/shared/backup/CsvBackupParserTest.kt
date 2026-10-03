package com.felixj.moneta.shared.backup

import com.felixj.moneta.settings.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CsvBackupParserTest {

    private fun sampleRow() = CsvBackupParser.BackupRow(
        name = "Lunch",
        amount = 50000L,
        currency = Currency.IDR,
        datetime = "2026-09-01T05:30:00Z",
        category = "Food",
        categoryType = "EXPENSE",
        categoryIcon = "baseline_fastfood_24",
        notes = "with friends"
    )

    @Test
    fun export_import_roundtrip_preservesRows() {
        val rows = listOf(
            sampleRow(),
            sampleRow().copy(
                name = "Salary",
                amount = 5000000L,
                datetime = "2026-09-01T02:00:00Z",
                category = "Salary",
                categoryType = "INCOME",
                categoryIcon = "baseline_account_balance_wallet_24",
                notes = "September"
            )
        )

        val parsed = CsvBackupParser.parse(CsvBackupParser.export(Currency.IDR, rows))

        assertEquals(Currency.IDR, parsed.currency)
        assertEquals(0, parsed.skipped)
        assertEquals(rows, parsed.rows)
    }

    @Test
    fun export_import_roundtrip_preservesSpecialCharacters() {
        val rows = listOf(
            sampleRow().copy(
                name = "Lunch, with \"friends\"",
                notes = "ate well,\n\"no\" regrets"
            )
        )

        val parsed = CsvBackupParser.parse(CsvBackupParser.export(Currency.IDR, rows))

        assertEquals(0, parsed.skipped)
        assertEquals(rows, parsed.rows)
    }

    @Test
    fun parse_skipsBadRows_andCountsThem() {
        val text = buildString {
            append("# currency=IDR\n")
            append(CsvBackupParser.HEADER_LINE + "\n")
            append(CsvBackupParser.toCsvRow(sampleRow()) + "\n")
            append("only,two,columns\n")
            append("Dinner,not_a_number,IDR,2026-09-01T05:30:00Z,Food,EXPENSE,baseline_fastfood_24,\n")
            append("Dinner,100,IDR,2026-13-01T05:30:00Z,Food,EXPENSE,baseline_fastfood_24,\n")
            append("Dinner,100,IDR,not-a-datetime,Food,EXPENSE,baseline_fastfood_24,\n")
            append("Dinner,100,XXX,2026-09-01T05:30:00Z,Food,EXPENSE,baseline_fastfood_24,\n")
            append(",100,IDR,2026-09-01T05:30:00Z,Food,EXPENSE,baseline_fastfood_24,\n")
            append("Dinner,100,IDR,2026-09-01T05:30:00Z,,EXPENSE,baseline_fastfood_24,\n")
            append("Dinner,100,IDR,2026-09-01T05:30:00Z,Food,SAVING,baseline_fastfood_24,\n")
            append("Dinner,100,IDR,\"2026-09-01T05:30:00Z,Food,EXPENSE,baseline_fastfood_24,\n")
        }

        val parsed = CsvBackupParser.parse(text)

        assertEquals(1, parsed.rows.size)
        assertEquals(9, parsed.skipped)
        assertEquals(sampleRow(), parsed.rows[0])
    }

    @Test
    fun fromCsvRow_parsesQuotedFields() {
        val row = CsvBackupParser.fromCsvRow(
            "\"Lunch, special\",50000,IDR,2026-09-01T05:30:00Z,Food,EXPENSE,baseline_fastfood_24,\"note with \"\"quotes\"\"\""
        )

        assertEquals("Lunch, special", row?.name)
        assertEquals("note with \"quotes\"", row?.notes)
    }

    @Test
    fun fromCsvRow_unknownIcon_fallsBackToLightbulb() {
        val row = CsvBackupParser.fromCsvRow(
            "Lunch,50000,IDR,2026-09-01T05:30:00Z,Food,EXPENSE,not_a_real_icon,some notes"
        )

        assertEquals("baseline_lightbulb_24", row?.categoryIcon)
    }

    @Test
    fun fromCsvRow_rejectsNonIsoDatetime() {
        assertNull(CsvBackupParser.fromCsvRow("Lunch,50000,IDR,2026-09-01 12:30,Food,EXPENSE,baseline_fastfood_24,"))
        assertNull(CsvBackupParser.fromCsvRow("Lunch,50000,IDR,2026-09-01,Food,EXPENSE,baseline_fastfood_24,"))
        assertNull(CsvBackupParser.fromCsvRow("Lunch,50000,IDR,2026-02-30T12:30:00Z,Food,EXPENSE,baseline_fastfood_24,"))
    }

    @Test
    fun parse_readsCurrencyHeader_forMismatchPrompt() {
        val idrFile = CsvBackupParser.export(Currency.IDR, listOf(sampleRow()))
        val usdFile = CsvBackupParser.export(Currency.USD, listOf(sampleRow().copy(currency = Currency.USD)))

        assertEquals(Currency.IDR, CsvBackupParser.parse(idrFile).currency)
        assertEquals(Currency.USD, CsvBackupParser.parse(usdFile).currency)
    }

    @Test
    fun parse_emptyFile_returnsNoRows() {
        val parsed = CsvBackupParser.parse("")

        assertEquals(0, parsed.rows.size)
        assertEquals(0, parsed.skipped)
        assertNull(parsed.currency)
    }
}
