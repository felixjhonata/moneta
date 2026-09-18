package com.felixj.moneta.add_activity.viewmodel

import com.felixj.moneta.R
import com.felixj.moneta.add_activity.model.AddActivityPageUiEvent
import com.felixj.moneta.add_activity.model.AddActivityPageUserEvent
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.Category
import com.felixj.moneta.shared.room.entity.CategoryType
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddActivityPageViewModelTest {

    private val categoryRepository = mockk<CategoryRepository>()
    private val activityRepository = mockk<ActivityRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { categoryRepository.getCategories() } returns listOf(
            Category(1, "Food & Drinks", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE),
            Category(2, "Salary", R.drawable.baseline_account_balance_wallet_24, CategoryType.INCOME)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submit_withAllFieldsValid_insertsActivityAndNavigatesBack() = runTest {
        coEvery { activityRepository.getNextActivityId() } returns 99
        coEvery { activityRepository.insertActivity(any()) } returns Unit

        val viewModel = AddActivityPageViewModel(categoryRepository, activityRepository)
        var emittedNavigateBack = false
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { event ->
                if (event == AddActivityPageUiEvent.NavigateBack) emittedNavigateBack = true
            }
        }

        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateAmount("1200000"))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateDate("18092026"))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateTime("1400"))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateNotes("Makan di luar"))
        viewModel.onUserEvent(AddActivityPageUserEvent.Submit)

        coVerify(exactly = 1) {
            activityRepository.insertActivity(
                Activity(99, 1, "Food & Drinks", "2026-09-18T14:00:00Z", 1200000, "Makan di luar")
            )
        }
        assertTrue(emittedNavigateBack)
    }

    @Test
    fun submit_withEmptyFields_setsErrorsAndDoesNotInsert() = runTest {
        val viewModel = AddActivityPageViewModel(categoryRepository, activityRepository)
        viewModel.onUserEvent(AddActivityPageUserEvent.Submit)

        val state = viewModel.uiState.value
        assertTrue(state.amountError)
        assertTrue(state.categoryError)
        assertTrue(state.dateError)
        assertTrue(state.timeError)
        coVerify(exactly = 0) { activityRepository.insertActivity(any()) }
    }

    @Test
    fun submit_withInvalidDateOrZeroAmount_setsOnlyRelevantErrors() = runTest {
        val viewModel = AddActivityPageViewModel(categoryRepository, activityRepository)
        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateAmount("0"))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateDate("31022026"))
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateTime("1400"))
        viewModel.onUserEvent(AddActivityPageUserEvent.Submit)

        val state = viewModel.uiState.value
        assertTrue(state.amountError)
        assertTrue(state.dateError)
        assertFalse(state.categoryError)
        assertFalse(state.timeError)
        coVerify(exactly = 0) { activityRepository.insertActivity(any()) }
    }

    @Test
    fun submit_withValidInputAfterError_clearsErrorsOnEdit() = runTest {
        val viewModel = AddActivityPageViewModel(categoryRepository, activityRepository)
        viewModel.onUserEvent(AddActivityPageUserEvent.Submit)
        viewModel.onUserEvent(AddActivityPageUserEvent.UpdateAmount("5000"))

        assertFalse(viewModel.uiState.value.amountError)
        assertTrue(viewModel.uiState.value.categoryError)
    }
}