package com.felixj.moneta.dashboard.viewmodel

import com.felixj.moneta.R
import com.felixj.moneta.dashboard.model.DashboardPageUserEvent
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import com.felixj.moneta.shared.util.DateUtil
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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

    private val repository = mockk<ActivityRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkObject(DateUtil)
        every { DateUtil.getLocalMonthAsUtcRange(any()) } returns Pair("2026-09-01T00:00:00Z", "2026-10-01T00:00:00Z")
        every { DateUtil.formatForDisplay(any(), any()) } answers { callOriginal() }
        every { DateUtil.formatForDisplay(any()) } answers { callOriginal() }
    }

    @After
    fun tearDown() {
        unmockkObject(DateUtil)
        Dispatchers.resetMain()
    }

    @Test
    fun loadData_withEmptyDatabase_setsZeroAmountsAndHidesSeeMore() = runTest {
        coEvery { repository.getActivities(3) } returns emptyList()
        coEvery { repository.getCurrentBalance() } returns 0L
        coEvery {
            repository.getTotalAmountByTypeAndDateRange(
                CategoryType.INCOME,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z"
            )
        } returns 0L
        coEvery {
            repository.getTotalAmountByTypeAndDateRange(
                CategoryType.EXPENSE,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z"
            )
        } returns 0L

        val viewModel = DashboardPageViewModel(repository)
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
        val sampleRecentActivities = listOf(
            ActivityWithCategoryIcon(
                Activity(3, 1, "Electricity Sep Newer", "2026-09-05T00:00:00Z", 500000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(1, 4, "Salary Sep Older", "2026-09-01T00:00:00Z", 5000000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.INCOME
            ),
            ActivityWithCategoryIcon(
                Activity(4, 1, "Water Aug", "2026-08-05T00:00:00Z", 300000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            )
        )
        coEvery { repository.getActivities(3) } returns sampleRecentActivities
        coEvery { repository.getCurrentBalance() } returns 8200000L
        coEvery {
            repository.getTotalAmountByTypeAndDateRange(
                CategoryType.INCOME,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z"
            )
        } returns 5000000L
        coEvery {
            repository.getTotalAmountByTypeAndDateRange(
                CategoryType.EXPENSE,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z"
            )
        } returns 500000L

        val viewModel = DashboardPageViewModel(repository)
        viewModel.onUserEvent(DashboardPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value

        assertEquals(UiText.CurrencyAmount(8200000L), uiState.currentBalance)
        assertEquals(UiText.CurrencyAmount(5000000L), uiState.income)
        assertEquals(UiText.CurrencyAmount(500000L), uiState.expense)
        assertEquals(3, uiState.recentActivities.size)
        assertEquals("Electricity Sep Newer", (uiState.recentActivities[0].activityLabel as UiText.DynamicString).value)
        assertEquals("Salary Sep Older", (uiState.recentActivities[1].activityLabel as UiText.DynamicString).value)
        assertTrue(uiState.showSeeMoreButton)
    }

    @Test
    fun loadData_withNegativeBalance_setsNegativePrefix() = runTest {
        val sampleActivities = listOf(
            ActivityWithCategoryIcon(
                Activity(1, 1, "Rent", "2026-09-01T00:00:00Z", 2000000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            )
        )
        coEvery { repository.getActivities(3) } returns sampleActivities
        coEvery { repository.getCurrentBalance() } returns -2000000L
        coEvery {
            repository.getTotalAmountByTypeAndDateRange(
                CategoryType.INCOME,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z"
            )
        } returns 0L
        coEvery {
            repository.getTotalAmountByTypeAndDateRange(
                CategoryType.EXPENSE,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z"
            )
        } returns 2000000L

        val viewModel = DashboardPageViewModel(repository)
        viewModel.onUserEvent(DashboardPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value

        assertEquals(UiText.CurrencyAmount(-2000000L, "- "), uiState.currentBalance)
        assertEquals(UiText.CurrencyAmount(0L), uiState.income)
        assertEquals(UiText.CurrencyAmount(2000000L), uiState.expense)
        assertTrue(uiState.showSeeMoreButton)
    }
}
