package com.felixj.moneta.dashboard.viewmodel

import com.felixj.moneta.R
import com.felixj.moneta.dashboard.model.DashboardPageUserEvent
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.room.dao.ActivityDao
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.ActivityType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardPageViewModelTest {

    private class FakeActivityDao(
        private val activities: List<Activity> = emptyList()
    ) : ActivityDao {
        override suspend fun getActivities(limit: Int): List<ActivityWithCategoryIcon> {
            val sorted = activities.sortedByDescending { it.date }
            val list = if (limit < 0) sorted else sorted.take(limit)
            return list.map { ActivityWithCategoryIcon(it, R.drawable.baseline_lightbulb_24) }
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
            return activities
                .filter { it.type == type && it.date >= startOfMonth && it.date < startOfNextMonth }
                .sumOf { it.amount }
        }

        override suspend fun insertAll(vararg activities: Activity) {}
        override suspend fun update(activity: Activity) {}
        override suspend fun delete(activity: Activity) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadData_withEmptyDatabase_setsZeroAmountsAndHidesSeeMore() = runTest {
        val fakeDao = FakeActivityDao(emptyList())
        val repository = ActivityRepository(fakeDao)
        val viewModel = DashboardPageViewModel(repository)
        viewModel.dateRangeProvider = { Pair("2026-09-01T00:00:00Z", "2026-10-01T00:00:00Z") }

        viewModel.onUserEvent(DashboardPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value

        assertEquals(UiText.CurrencyAmount(0L), uiState.currentBalance)
        assertEquals(UiText.CurrencyAmount(0L), uiState.income)
        assertEquals(UiText.CurrencyAmount(0L), uiState.expense)
        assertTrue(uiState.recentActivities.isEmpty())
        assertFalse(uiState.showSeeMoreButton)
    }

    @Test
    fun loadData_withActivities_calculatesBalanceEarnedSpentAndShowsSeeMore() = runTest {
        val sampleActivities = listOf(
            Activity(1, 4, "Salary Sep Older", "2026-09-01T00:00:00Z", 5000000, ActivityType.INCOME, ""),
            Activity(2, 4, "Salary Aug", "2026-08-01T00:00:00Z", 4000000, ActivityType.INCOME, ""),
            Activity(3, 1, "Electricity Sep Newer", "2026-09-05T00:00:00Z", 500000, ActivityType.EXPENSE, ""),
            Activity(4, 1, "Water Aug", "2026-08-05T00:00:00Z", 300000, ActivityType.EXPENSE, "")
        )
        val fakeDao = FakeActivityDao(sampleActivities)
        val repository = ActivityRepository(fakeDao)
        val viewModel = DashboardPageViewModel(repository)
        viewModel.dateRangeProvider = { Pair("2026-09-01T00:00:00Z", "2026-10-01T00:00:00Z") }

        viewModel.onUserEvent(DashboardPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value

        // Total income = 5M + 4M = 9M, Total expense = 500k + 300k = 800k -> Balance = 8.2M
        assertEquals(UiText.CurrencyAmount(8200000L), uiState.currentBalance)
        // Earned this month (Sep) = 5M
        assertEquals(UiText.CurrencyAmount(5000000L), uiState.income)
        // Spent this month (Sep) = 500k
        assertEquals(UiText.CurrencyAmount(500000L), uiState.expense)
        // Recent activities up to 3
        assertEquals(3, uiState.recentActivities.size)
        // Verify order: newest first
        assertEquals("Electricity Sep Newer", (uiState.recentActivities[0].activityLabel as UiText.DynamicString).value)
        assertEquals("Salary Sep Older", (uiState.recentActivities[1].activityLabel as UiText.DynamicString).value)
        // Recent activities not empty -> showSeeMoreButton is true
        assertTrue(uiState.showSeeMoreButton)
    }

    @Test
    fun loadData_withNegativeBalance_setsNegativePrefix() = runTest {
        val sampleActivities = listOf(
            Activity(1, 1, "Rent", "2026-09-01T00:00:00Z", 2000000, ActivityType.EXPENSE, "")
        )
        val fakeDao = FakeActivityDao(sampleActivities)
        val repository = ActivityRepository(fakeDao)
        val viewModel = DashboardPageViewModel(repository)
        viewModel.dateRangeProvider = { Pair("2026-09-01T00:00:00Z", "2026-10-01T00:00:00Z") }

        viewModel.onUserEvent(DashboardPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value

        assertEquals(UiText.CurrencyAmount(-2000000L, "- "), uiState.currentBalance)
        assertEquals(UiText.CurrencyAmount(0L), uiState.income)
        assertEquals(UiText.CurrencyAmount(2000000L), uiState.expense)
        assertTrue(uiState.showSeeMoreButton)
    }
}
