package com.felixj.moneta.shared.repository

import com.felixj.moneta.R
import com.felixj.moneta.shared.room.dao.ActivityDao
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.ActivityType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityRepositoryTest {

    private class FakeActivityDao(
        private val activities: List<Activity> = emptyList(),
        private val categoryIconProvider: (Activity) -> Int = { R.drawable.baseline_lightbulb_24 }
    ) : ActivityDao {
        var lastLimitPassed: Int? = null
        var lastTypePassed: ActivityType? = null
        var lastStartOfMonthPassed: String? = null
        var lastStartOfNextMonthPassed: String? = null

        override suspend fun getActivities(limit: Int): List<ActivityWithCategoryIcon> {
            lastLimitPassed = limit
            val sorted = activities.sortedByDescending { it.date }
            val list = if (limit < 0) sorted else sorted.take(limit)
            return list.map { ActivityWithCategoryIcon(it, categoryIconProvider(it)) }
        }

        override suspend fun getCurrentBalance(): Long {
            val income = activities.filter { it.type == ActivityType.INCOME }.sumOf { it.amount }
            val expense = activities.filter { it.type == ActivityType.EXPENSE }.sumOf { it.amount }
            return income - expense
        }

        override suspend fun getTotalAmountByTypeAndDateRange(
            type: ActivityType,
            startOfMonth: String,
            startOfNextMonth: String
        ): Long {
            lastTypePassed = type
            lastStartOfMonthPassed = startOfMonth
            lastStartOfNextMonthPassed = startOfNextMonth
            return activities
                .filter { it.type == type && it.date >= startOfMonth && it.date < startOfNextMonth }
                .sumOf { it.amount }
        }

        override suspend fun insertAll(vararg activities: Activity) {}
        override suspend fun update(activity: Activity) {}
        override suspend fun delete(activity: Activity) {}
    }

    @Test
    fun getActivities_delegatesToDaoAndReturnsMostRecent() = runTest {
        val sampleActivities = listOf(
            Activity(1, 1, "Electricity Older", "2026-09-01T00:00:00Z", 500000, ActivityType.EXPENSE, ""),
            Activity(2, 4, "Salary Newer", "2026-09-05T00:00:00Z", 5000000, ActivityType.INCOME, "")
        )
        val fakeDao = FakeActivityDao(sampleActivities)
        val repository = ActivityRepository(fakeDao)

        val result = repository.getActivities(1)

        assertEquals(1, fakeDao.lastLimitPassed)
        assertEquals(1, result.size)
        assertEquals("Salary Newer", result[0].activity.name)
    }

    @Test
    fun getCurrentBalance_delegatesToDao() = runTest {
        val sampleActivities = listOf(
            Activity(1, 4, "Salary", "2026-09-01T00:00:00Z", 5000000, ActivityType.INCOME, ""),
            Activity(2, 4, "Bonus", "2026-09-10T00:00:00Z", 1000000, ActivityType.INCOME, ""),
            Activity(3, 1, "Bill", "2026-09-02T00:00:00Z", 300000, ActivityType.EXPENSE, "")
        )
        val fakeDao = FakeActivityDao(sampleActivities)
        val repository = ActivityRepository(fakeDao)

        val balance = repository.getCurrentBalance()

        assertEquals(5700000L, balance)
    }

    @Test
    fun getTotalAmountByTypeAndDateRange_delegatesToDao() = runTest {
        val sampleActivities = listOf(
            Activity(1, 4, "Salary Sep", "2026-09-01T00:00:00Z", 5000000, ActivityType.INCOME, ""),
            Activity(2, 4, "Salary Aug", "2026-08-01T00:00:00Z", 4500000, ActivityType.INCOME, "")
        )
        val fakeDao = FakeActivityDao(sampleActivities)
        val repository = ActivityRepository(fakeDao)

        val result = repository.getTotalAmountByTypeAndDateRange(
            ActivityType.INCOME,
            "2026-09-01T00:00:00Z",
            "2026-10-01T00:00:00Z"
        )

        assertEquals(ActivityType.INCOME, fakeDao.lastTypePassed)
        assertEquals("2026-09-01T00:00:00Z", fakeDao.lastStartOfMonthPassed)
        assertEquals("2026-10-01T00:00:00Z", fakeDao.lastStartOfNextMonthPassed)
        assertEquals(5000000L, result)
    }
}
