package com.felixj.moneta.shared.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateUtil {
    private const val DISPLAY_PATTERN = "d MMMM yyyy"

    private val ISO_DATE_TIME_PATTERNS = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    private val ISO_DATE_PATTERNS = listOf(
        "yyyy-MM-dd"
    )

    fun getLocalMonthAsUtcRange(
        calendar: Calendar = Calendar.getInstance()
    ): Pair<String, String> {
        val startCal = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nextCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return Pair(sdf.format(startCal.time), sdf.format(nextCal.time))
    }

    fun formatForDisplay(
        dateString: String,
        targetTimeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val trimmed = dateString.trim()
        if (trimmed.isEmpty()) return ""

        val utcTimeZone = TimeZone.getTimeZone("UTC")

        if (trimmed.contains("T")) {
            for (pattern in ISO_DATE_TIME_PATTERNS) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                        timeZone = utcTimeZone
                        isLenient = false
                    }
                    val parsed = sdf.parse(trimmed)
                    if (parsed != null) {
                        val displayFormat = SimpleDateFormat(DISPLAY_PATTERN, Locale.ENGLISH).apply {
                            timeZone = targetTimeZone
                        }
                        return displayFormat.format(parsed)
                    }
                } catch (_: Exception) {
                    // Try next pattern
                }
            }
        } else {
            for (pattern in ISO_DATE_PATTERNS) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                        isLenient = false
                    }
                    val parsed = sdf.parse(trimmed)
                    if (parsed != null) {
                        val displayFormat = SimpleDateFormat(DISPLAY_PATTERN, Locale.ENGLISH)
                        return displayFormat.format(parsed)
                    }
                } catch (_: Exception) {
                    // Try next pattern
                }
            }
        }
        return trimmed
    }

    fun formatRelativeDate(
        dateString: String,
        nowCalendar: Calendar = Calendar.getInstance(),
        targetTimeZone: TimeZone = nowCalendar.timeZone
    ): String {
        val formattedDate = formatForDisplay(dateString, targetTimeZone)
        if (formattedDate.isEmpty()) return ""

        val displayFormat = SimpleDateFormat(DISPLAY_PATTERN, Locale.ENGLISH).apply {
            timeZone = targetTimeZone
        }
        val todayCal = (nowCalendar.clone() as Calendar).apply {
            timeZone = targetTimeZone
        }
        val todayString = displayFormat.format(todayCal.time)

        val yesterdayCal = (todayCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayString = displayFormat.format(yesterdayCal.time)

        return when (formattedDate) {
            todayString -> "Today"
            yesterdayString -> "Yesterday"
            else -> formattedDate
        }
    }

    fun isValidDateInput(date: String): Boolean = toIsoUtcDateTime(date, "0000") != null

    fun isValidTimeInput(time: String): Boolean = toIsoUtcDateTime("01012000", time) != null

    /**
     * Combines a `ddMMyyyy` date and `HHmm` time into the UTC ISO datetime string stored
     * for activities (e.g. "2026-09-18T14:00:00Z"). Returns null if either input is not
     * a real calendar date/time.
     */
    fun toIsoUtcDateTime(date: String, time: String): String? {
        if (date.length != 8 || time.length != 4) return null
        val day = date.substring(0, 2).toIntOrNull() ?: return null
        val month = date.substring(2, 4).toIntOrNull() ?: return null
        val year = date.substring(4, 8).toIntOrNull() ?: return null
        val hour = time.substring(0, 2).toIntOrNull() ?: return null
        val minute = time.substring(2, 4).toIntOrNull() ?: return null
        if (day !in 1..31 || month !in 1..12 || year !in 0..9999 || hour !in 0..23 || minute !in 0..59) return null

        val valid = try {
            val calendar = Calendar.getInstance().apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
                clear()
                set(year, month - 1, day, hour, minute, 0)
                getTimeInMillis()
            }
            calendar.get(Calendar.DAY_OF_MONTH) == day &&
                calendar.get(Calendar.MONTH) == month - 1 &&
                calendar.get(Calendar.YEAR) == year
        } catch (_: Exception) {
            false
        }
        if (!valid) return null

        return String.format(
            Locale.US,
            "%04d-%02d-%02dT%02d:%02d:00Z",
            year, month, day, hour, minute
        )
    }
}
