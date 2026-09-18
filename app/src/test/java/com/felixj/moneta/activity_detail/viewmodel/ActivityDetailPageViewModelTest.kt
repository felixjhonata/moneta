package com.felixj.moneta.activity_detail.viewmodel

import com.felixj.moneta.R
import com.felixj.moneta.activity_detail.model.ActivityDetailPageUiEvent
import com.felixj.moneta.activity_detail.model.ActivityDetailPageUserEvent
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityDetailPageViewModelTest {

    private val repository = mockk<ActivityRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkObject(DateUtil)
        every { DateUtil.formatForDisplay(any()) } returns "4 September 2026"
        every { DateUtil.formatTimeForDisplay(any()) } returns "14:00"
    }

    @After
    fun tearDown() {
        unmockkObject(DateUtil)
        Dispatchers.resetMain()
    }

    @Test
    fun loadData_withExpenseActivity_populatesUiState() = runTest {
        coEvery { repository.getActivity(5) } returns ActivityWithCategoryIcon(
            Activity(5, 3, "Electricity Bills", "2026-09-04T14:00:00Z", 1200000, "Monthly bill"),
            R.drawable.baseline_lightbulb_24,
            CategoryType.EXPENSE
        )

        val viewModel = ActivityDetailPageViewModel(repository, MonetaRoute.ActivityDetail(5))
        viewModel.onUserEvent(ActivityDetailPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value
        assertEquals(R.drawable.baseline_lightbulb_24, uiState.icon)
        assertEquals("Electricity Bills", uiState.name)
        assertEquals(CategoryType.EXPENSE, uiState.categoryType)
        assertEquals(UiText.CurrencyAmount(1200000, "- "), uiState.amount)
        assertEquals("4 September 2026", uiState.date)
        assertEquals("14:00", uiState.time)
        assertEquals("Monthly bill", uiState.note)
    }

    @Test
    fun loadData_withIncomeActivity_usesPositivePrefix() = runTest {
        coEvery { repository.getActivity(9) } returns ActivityWithCategoryIcon(
            Activity(9, 6, "Salary", "2026-09-01T08:00:00Z", 5000000, ""),
            R.drawable.baseline_account_balance_wallet_24,
            CategoryType.INCOME
        )

        val viewModel = ActivityDetailPageViewModel(repository, MonetaRoute.ActivityDetail(9))
        viewModel.onUserEvent(ActivityDetailPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value
        assertEquals(CategoryType.INCOME, uiState.categoryType)
        assertEquals(UiText.CurrencyAmount(5000000, "+ "), uiState.amount)
    }

    @Test
    fun loadData_withMissingActivity_keepsDefaultUiState() = runTest {
        coEvery { repository.getActivity(999) } returns null

        val viewModel = ActivityDetailPageViewModel(repository, MonetaRoute.ActivityDetail(999))
        viewModel.onUserEvent(ActivityDetailPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value
        assertEquals("", uiState.name)
        assertEquals(null, uiState.icon)
        assertEquals(UiText.CurrencyAmount(0L), uiState.amount)
    }

    @Test
    fun navigateBack_emitsNavigateBackEvent() = runTest {
        val viewModel = ActivityDetailPageViewModel(repository, MonetaRoute.ActivityDetail(1))
        val event = async(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEvent.first() }
        viewModel.onUserEvent(ActivityDetailPageUserEvent.NavigateBack)

        assertEquals(ActivityDetailPageUiEvent.NavigateBack, event.await())
    }
}