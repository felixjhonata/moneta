package com.felixj.moneta.shared.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateUtilTest {

    @Test
    fun getLocalMonthAsUtcRange_inUtc_returnsFirstDayToNextMonthFirstDay() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 14)
            set(Calendar.HOUR_OF_DAY, 12)
        }

        val (start, next) = DateUtil.getLocalMonthAsUtcRange(calendar)

        assertEquals("2026-09-01T00:00:00Z", start)
        assertEquals("2026-10-01T00:00:00Z", next)
    }

    @Test
    fun getLocalMonthAsUtcRange_inPlus7_returnsAdjustedUtcRange() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 14)
            set(Calendar.HOUR_OF_DAY, 9)
        }

        val (start, next) = DateUtil.getLocalMonthAsUtcRange(calendar)

        assertEquals("2026-08-31T17:00:00Z", start)
        assertEquals("2026-09-30T17:00:00Z", next)
    }

    @Test
    fun formatForDisplay_formatsIsoDate() {
        val isoDate = "2026-09-14"
        val formatted = DateUtil.formatForDisplay(isoDate)

        assertEquals("14 September 2026", formatted)
    }

    @Test
    fun formatForDisplay_convertsUtcToLocalTimezone() {
        // 23:30 on August 31st UTC is 06:30 on September 1st in GMT+7
        val utcIso = "2026-08-31T23:30:00Z"
        val formatted = DateUtil.formatForDisplay(utcIso, TimeZone.getTimeZone("GMT+7"))

        assertEquals("1 September 2026", formatted)
    }

    @Test
    fun formatForDisplay_convertsUtcToWesternTimezone() {
        // 01:00 on September 1st UTC is 20:00 on August 31st in GMT-5 (EST)
        val utcIso = "2026-09-01T01:00:00Z"
        val formatted = DateUtil.formatForDisplay(utcIso, TimeZone.getTimeZone("GMT-5"))

        assertEquals("31 August 2026", formatted)
    }

    @Test
    fun formatForDisplay_formatsIsoDateTimeWithMillisecondsAndOffset() {
        val isoDateTime = "2026-09-14T10:30:00.000Z"
        val formatted = DateUtil.formatForDisplay(isoDateTime, TimeZone.getTimeZone("UTC"))

        assertEquals("14 September 2026", formatted)
    }

    @Test
    fun formatForDisplay_preservesAlreadyFormattedDate() {
        val alreadyFormatted = "12 April 2026"
        val formatted = DateUtil.formatForDisplay(alreadyFormatted)

        assertEquals("12 April 2026", formatted)
    }

    @Test
    fun formatForDisplay_returnsOriginalOnInvalidDate() {
        val invalid = "not-a-date"
        val formatted = DateUtil.formatForDisplay(invalid)

        assertEquals("not-a-date", formatted)
    }

    @Test
    fun formatRelativeDate_today_returnsToday() {
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 12)
        }

        val todayDate = "2026-09-15T08:00:00Z"
        val result = DateUtil.formatRelativeDate(todayDate, now, TimeZone.getTimeZone("UTC"))

        assertEquals("Today", result)
    }

    @Test
    fun formatRelativeDate_yesterday_returnsYesterday() {
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 12)
        }

        val yesterdayDate = "2026-09-14T20:00:00Z"
        val result = DateUtil.formatRelativeDate(yesterdayDate, now, TimeZone.getTimeZone("UTC"))

        assertEquals("Yesterday", result)
    }

    @Test
    fun formatRelativeDate_olderDate_returnsFormattedDate() {
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 12)
        }

        val olderDate = "2026-08-04T10:00:00Z"
        val result = DateUtil.formatRelativeDate(olderDate, now, TimeZone.getTimeZone("UTC"))

        assertEquals("4 August 2026", result)
    }

    @Test
    fun formatRelativeDate_withTimezoneShift_evaluatesCorrectDay() {
        // 2026-09-14 23:30 UTC is 2026-09-15 06:30 in GMT+7
        val now = Calendar.getInstance(TimeZone.getTimeZone("GMT+7")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 10)
        }

        val utcIso = "2026-09-14T23:30:00Z"
        val result = DateUtil.formatRelativeDate(utcIso, now, TimeZone.getTimeZone("GMT+7"))

        assertEquals("Today", result)
    }
}
