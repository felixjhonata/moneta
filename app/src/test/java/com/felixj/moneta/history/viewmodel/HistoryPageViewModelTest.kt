package com.felixj.moneta.history.viewmodel

import com.felixj.moneta.R
import com.felixj.moneta.history.model.HistoryListItemUiModel
import com.felixj.moneta.history.model.HistoryPageUiEvent
import com.felixj.moneta.history.model.HistoryPageUserEvent
import com.felixj.moneta.shared.model.MonetaRoute
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryPageViewModelTest {

    private val repository = mockk<ActivityRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkObject(DateUtil)
        every { DateUtil.formatForDisplay(any(), any()) } answers { callOriginal() }
        every { DateUtil.formatForDisplay(any()) } answers { callOriginal() }
    }

    @After
    fun tearDown() {
        unmockkObject(DateUtil)
        Dispatchers.resetMain()
    }

    @Test
    fun loadData_withEmptyDatabase_setsEmptyList() = runTest {
        coEvery { repository.getActivities() } returns emptyList()

        val viewModel = HistoryPageViewModel(repository)
        viewModel.onUserEvent(HistoryPageUserEvent.LoadData)

        val items = viewModel.uiState.value.historyListItems
        assertTrue(items.isEmpty())
    }

    @Test
    fun loadData_withActivities_groupsByDateAndInsertsSingleHeaderPerDate() = runTest {
        val sampleActivities = listOf(
            ActivityWithCategoryIcon(
                Activity(1, 1, "Lunch Today", "2026-09-15T12:00:00Z", 50000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(2, 4, "Salary Today", "2026-09-15T08:00:00Z", 5000000, ""),
                R.drawable.baseline_account_balance_wallet_24,
                CategoryType.INCOME
            ),
            ActivityWithCategoryIcon(
                Activity(3, 1, "Dinner Yesterday", "2026-09-14T19:00:00Z", 100000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(4, 1, "Book Older", "2026-08-04T15:00:00Z", 80000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(5, 1, "Coffee Older", "2026-08-04T09:00:00Z", 35000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            )
        )
        coEvery { repository.getActivities() } returns sampleActivities

        every { DateUtil.formatRelativeDate(eq("2026-09-15T12:00:00Z"), any(), any()) } returns "Today"
        every { DateUtil.formatRelativeDate(eq("2026-09-15T08:00:00Z"), any(), any()) } returns "Today"
        every { DateUtil.formatRelativeDate(eq("2026-09-14T19:00:00Z"), any(), any()) } returns "Yesterday"
        every { DateUtil.formatRelativeDate(eq("2026-08-04T15:00:00Z"), any(), any()) } returns "4 August 2026"
        every { DateUtil.formatRelativeDate(eq("2026-08-04T09:00:00Z"), any(), any()) } returns "4 August 2026"

        val viewModel = HistoryPageViewModel(repository)
        viewModel.onUserEvent(HistoryPageUserEvent.LoadData)

        val items = viewModel.uiState.value.historyListItems
        assertEquals(8, items.size)

        // Date headers
        assertEquals(HistoryListItemUiModel.Date("Today"), items[0])
        assertEquals(HistoryListItemUiModel.Date("Yesterday"), items[3])
        assertEquals(HistoryListItemUiModel.Date("4 August 2026"), items[5])

        // Activity item 1 (Expense)
        val lunchItem = items[1] as HistoryListItemUiModel.ActivityItem
        assertEquals("Lunch Today", (lunchItem.itemUiModel.activityLabel as UiText.DynamicString).value)
        assertEquals(UiText.CurrencyAmount(50000, "- "), lunchItem.itemUiModel.amount)
        assertEquals(R.drawable.baseline_lightbulb_24, lunchItem.itemUiModel.icon)
        assertTrue(lunchItem.itemUiModel.isExpense)

        // Activity item 2 (Income)
        val salaryItem = items[2] as HistoryListItemUiModel.ActivityItem
        assertEquals("Salary Today", (salaryItem.itemUiModel.activityLabel as UiText.DynamicString).value)
        assertEquals(UiText.CurrencyAmount(5000000, "+ "), salaryItem.itemUiModel.amount)
        assertEquals(R.drawable.baseline_account_balance_wallet_24, salaryItem.itemUiModel.icon)
        assertEquals(false, salaryItem.itemUiModel.isExpense)

        // Activity item 3 (Yesterday)
        val dinnerItem = items[4] as HistoryListItemUiModel.ActivityItem
        assertEquals("Dinner Yesterday", (dinnerItem.itemUiModel.activityLabel as UiText.DynamicString).value)

        // Activity items 4 & 5 (Older)
        val bookItem = items[6] as HistoryListItemUiModel.ActivityItem
        assertEquals("Book Older", (bookItem.itemUiModel.activityLabel as UiText.DynamicString).value)
        val coffeeItem = items[7] as HistoryListItemUiModel.ActivityItem
        assertEquals("Coffee Older", (coffeeItem.itemUiModel.activityLabel as UiText.DynamicString).value)
    }

    @Test
    fun loadData_withUnsortedActivities_sortsChronologicallyDescending() = runTest {
        val unsortedActivities = listOf(
            ActivityWithCategoryIcon(
                Activity(1, 1, "Book Older", "2026-08-04T15:00:00Z", 80000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(2, 1, "Lunch Today", "2026-09-15T12:00:00Z", 50000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            )
        )
        coEvery { repository.getActivities() } returns unsortedActivities

        every { DateUtil.formatRelativeDate(eq("2026-09-15T12:00:00Z"), any(), any()) } returns "Today"
        every { DateUtil.formatRelativeDate(eq("2026-08-04T15:00:00Z"), any(), any()) } returns "4 August 2026"

        val viewModel = HistoryPageViewModel(repository)
        viewModel.onUserEvent(HistoryPageUserEvent.LoadData)

        val items = viewModel.uiState.value.historyListItems
        assertEquals(4, items.size)
        assertEquals(HistoryListItemUiModel.Date("Today"), items[0])
        assertEquals("Lunch Today", ((items[1] as HistoryListItemUiModel.ActivityItem).itemUiModel.activityLabel as UiText.DynamicString).value)
        assertEquals(HistoryListItemUiModel.Date("4 August 2026"), items[2])
        assertEquals("Book Older", ((items[3] as HistoryListItemUiModel.ActivityItem).itemUiModel.activityLabel as UiText.DynamicString).value)
    }

    @Test
    fun onUserEvent_navigateTo_emitsNavigationEvent() = runTest {
        val viewModel = HistoryPageViewModel(repository)

        var emittedDestination: MonetaRoute? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { event ->
                if (event is HistoryPageUiEvent.NavigateTo) {
                    emittedDestination = event.destination
                }
            }
        }

        viewModel.onUserEvent(HistoryPageUserEvent.NavigateTo(MonetaRoute.Settings))
        testScheduler.runCurrent()

        assertEquals(MonetaRoute.Settings, emittedDestination)
    }
}
