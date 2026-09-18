package com.felixj.moneta.activity.edit_activity.viewmodel

import com.felixj.moneta.R
import com.felixj.moneta.activity.shared.model.AddActivityPageUiEvent
import com.felixj.moneta.activity.shared.model.AddActivityPageUserEvent
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.Category
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class EditActivityPageViewModelTest {

    private val categoryRepository = mockk<CategoryRepository>()
    private val activityRepository = mockk<ActivityRepository>()
    private val originalTimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        coEvery { categoryRepository.getCategories() } returns listOf(
            Category(1, "Food & Drinks", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE),
            Category(2, "Salary", R.drawable.baseline_account_balance_wallet_24, CategoryType.INCOME),
            Category(3, "Utilities", R.drawable.baseline_lightbulb_24, CategoryType.EXPENSE)
        )
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
        Dispatchers.resetMain()
    }

    private fun storedActivity(activityId: Int = 5) = ActivityWithCategoryIcon(
        Activity(activityId, 3, "Utilities", "2026-09-04T14:00:00Z", 1200000, "Monthly bill"),
        R.drawable.baseline_lightbulb_24,
        CategoryType.EXPENSE
    )

    @Test
    fun loadData_populatesUiStateFromStoredActivity() = runTest {
        coEvery { activityRepository.getActivity(5) } returns storedActivity()

        val viewModel = EditActivityPageViewModel(categoryRepository, activityRepository, MonetaRoute.EditActivity(5))
        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)

        val uiState = viewModel.uiState.value
        assertEquals("1200000", uiState.amount)
        assertEquals(CategoryType.EXPENSE, uiState.categoryType)
        assertEquals(3, uiState.selectedCategoryId)
        assertEquals("04092026", uiState.date)
        assertEquals("1400", uiState.time)
        assertEquals("Monthly bill", uiState.note)
    }

    @Test
    fun loadData_withMissingActivity_keepsDefaultUiState() = runTest {
        coEvery { activityRepository.getActivity(999) } returns null

        val viewModel = EditActivityPageViewModel(categoryRepository, activityRepository, MonetaRoute.EditActivity(999))
        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)

        assertEquals("", viewModel.uiState.value.amount)
        assertEquals(null, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun submit_updatesActivityWithNewValuesAndNavigatesBack() = runTest {
        coEvery { activityRepository.getActivity(5) } returns storedActivity()
        coEvery { activityRepository.updateActivity(any()) } returns Unit

        val viewModel = EditActivityPageViewModel(categoryRepository, activityRepository, MonetaRoute.EditActivity(5))
        var emittedNavigateBack = false
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { event ->
                if (event == AddActivityPageUiEvent.NavigateBack) emittedNavigateBack = true
            }
        }

        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateAmount("1100000"))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateDate("10092026"))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateTime("1500"))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateNotes("Updated bill"))
        viewModel.onUserEvent(AddActivityPageUserEvent.Submit)

        coVerify(exactly = 1) {
            activityRepository.updateActivity(
                Activity(5, 3, "Utilities", "2026-09-10T15:00:00Z", 1100000, "Updated bill")
            )
        }
        assertTrue(emittedNavigateBack)
    }

    @Test
    fun submit_withEmptyFields_setsErrorsAndDoesNotUpdate() = runTest {
        coEvery { activityRepository.getActivity(5) } returns storedActivity()

        val viewModel = EditActivityPageViewModel(categoryRepository, activityRepository, MonetaRoute.EditActivity(5))
        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateAmount(""))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateDate(""))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateTime(""))
        viewModel.onUserEvent(AddActivityPageUserEvent.Submit)

        val state = viewModel.uiState.value
        assertTrue(state.amountError)
        assertTrue(state.dateError)
        assertTrue(state.timeError)
        coVerify(exactly = 0) { activityRepository.updateActivity(any()) }
    }

    @Test
    fun submit_withValidInputAfterError_clearsErrorsOnEdit() = runTest {
        coEvery { activityRepository.getActivity(5) } returns storedActivity()
        coEvery { activityRepository.updateActivity(any()) } returns Unit

        val viewModel = EditActivityPageViewModel(categoryRepository, activityRepository, MonetaRoute.EditActivity(5))
        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)
        viewModel.onUserEvent(AddActivityPageUserEvent.Submit)
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateAmount("5000"))

        assertFalse(viewModel.uiState.value.amountError)
    }
}