package com.felixj.moneta.history.viewmodel

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.testing.asSnapshot
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
import io.mockk.every
import io.mockk.mockk
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryPageViewModelTest {

    private val repository = mockk<ActivityRepository>()

    private class FakePagingSource(
        private val data: List<ActivityWithCategoryIcon>
    ) : PagingSource<Int, ActivityWithCategoryIcon>() {
        override fun getRefreshKey(state: PagingState<Int, ActivityWithCategoryIcon>): Int? = null

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ActivityWithCategoryIcon> {
            val key = params.key ?: 0
            val start = key.coerceIn(0, data.size)
            val end = min(start + params.loadSize, data.size)
            val page = if (start >= end) emptyList() else data.subList(start, end)
            return LoadResult.Page(
                data = page,
                prevKey = if (key == 0) null else max(0, key - params.loadSize),
                nextKey = if (end >= data.size) null else end
            )
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun isoNow(): String = isoFor(Calendar.getInstance())

    private fun isoYesterday(): String =
        isoFor(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) })

    private fun isoFor(cal: Calendar): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(cal.time)

    @Test
    fun pagedHistory_withEmptyDatabase_emitsEmptyList() = runTest {
        every { repository.getActivitiesPaged() } answers { FakePagingSource(emptyList()) }

        val viewModel = HistoryPageViewModel(repository)
        val items = viewModel.pagedHistory.asSnapshot()

        assertTrue(items.isEmpty())
    }

    @Test
    fun pagedHistory_withActivities_groupsByDateAndInsertsSingleHeaderPerDate() = runTest {
        val todayIso = isoNow()
        val yesterdayIso = isoYesterday()
        // ponytail: fixed old date at noon UTC stays on the same calendar day in every timezone
        val olderIso1 = "2020-01-01T12:00:00Z"
        val olderIso2 = "2020-01-01T09:00:00Z"
        val sampleActivities = listOf(
            ActivityWithCategoryIcon(
                Activity(1, 1, "Lunch Today", todayIso, 50000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(2, 4, "Salary Today", todayIso, 5000000, ""),
                R.drawable.baseline_account_balance_wallet_24,
                CategoryType.INCOME
            ),
            ActivityWithCategoryIcon(
                Activity(3, 1, "Dinner Yesterday", yesterdayIso, 100000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(4, 1, "Book Older", olderIso1, 80000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            ),
            ActivityWithCategoryIcon(
                Activity(5, 1, "Coffee Older", olderIso2, 35000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            )
        )
        every { repository.getActivitiesPaged() } answers { FakePagingSource(sampleActivities) }

        val viewModel = HistoryPageViewModel(repository)
        val items = viewModel.pagedHistory.asSnapshot()

        assertEquals(8, items.size)

        // Date headers (real DateUtil: Today / Yesterday / 1 January 2020)
        assertEquals(HistoryListItemUiModel.Date("Today"), items[0])
        assertEquals(HistoryListItemUiModel.Date("Yesterday"), items[3])
        assertEquals(HistoryListItemUiModel.Date("1 January 2020"), items[5])

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
    fun pagedHistory_withSameDateAcrossPageBoundaries_emitsSingleHeader() = runTest {
        // 25 same-day items fit in initial load; insertSeparators dedupes headers
        // across pages by comparing before/after, so one header proves the logic.
        val todayIso = isoNow()
        val manySameDay = (1..25).map { id ->
            ActivityWithCategoryIcon(
                Activity(id, 1, "Item $id", todayIso, 1000L + id, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.EXPENSE
            )
        }
        every { repository.getActivitiesPaged() } answers { FakePagingSource(manySameDay) }

        val viewModel = HistoryPageViewModel(repository)
        val items = viewModel.pagedHistory.asSnapshot()

        // 1 header + 25 activities, header must not duplicate
        assertEquals(26, items.size)
        assertEquals(HistoryListItemUiModel.Date("Today"), items[0])
        assertEquals(1, items.filterIsInstance<HistoryListItemUiModel.Date>().size)
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
