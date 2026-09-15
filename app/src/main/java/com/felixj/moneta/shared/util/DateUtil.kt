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
}
