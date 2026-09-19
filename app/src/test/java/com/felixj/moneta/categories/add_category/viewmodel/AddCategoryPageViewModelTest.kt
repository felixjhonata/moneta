package com.felixj.moneta.categories.add_category.viewmodel

import com.felixj.moneta.R
import com.felixj.moneta.categories.shared.model.AddCategoryPageUiEvent
import com.felixj.moneta.categories.shared.model.AddCategoryPageUserEvent
import com.felixj.moneta.shared.repository.CategoryRepository
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
class AddCategoryPageViewModelTest {

    private val categoryRepository = mockk<CategoryRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submit_withValidName_insertsCategoryAndNavigatesBack() = runTest {
        coEvery { categoryRepository.getNextCategoryId() } returns 99
        coEvery { categoryRepository.insertCategory(any()) } returns Unit

        val viewModel = AddCategoryPageViewModel(categoryRepository)
        var emittedNavigateBack = false
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { event ->
                if (event == AddCategoryPageUiEvent.NavigateBack) emittedNavigateBack = true
            }
        }

        viewModel.onUserEvent(AddCategoryPageUserEvent.UpdateName("Food"))
        viewModel.onUserEvent(AddCategoryPageUserEvent.SelectCategoryType(CategoryType.INCOME))
        viewModel.onUserEvent(AddCategoryPageUserEvent.SelectIcon(R.drawable.baseline_fastfood_24))
        viewModel.onUserEvent(AddCategoryPageUserEvent.Submit)

        coVerify(exactly = 1) {
            categoryRepository.insertCategory(
                Category(99, "Food", R.drawable.baseline_fastfood_24, CategoryType.INCOME)
            )
        }
        assertTrue(emittedNavigateBack)
    }

    @Test
    fun submit_withBlankName_setsErrorAndDoesNotInsert() = runTest {
        val viewModel = AddCategoryPageViewModel(categoryRepository)
        viewModel.onUserEvent(AddCategoryPageUserEvent.Submit)

        assertTrue(viewModel.uiState.value.nameError)
        coVerify(exactly = 0) { categoryRepository.insertCategory(any()) }
    }

    @Test
    fun updateName_afterError_clearsError() = runTest {
        val viewModel = AddCategoryPageViewModel(categoryRepository)
        viewModel.onUserEvent(AddCategoryPageUserEvent.Submit)
        viewModel.onUserEvent(AddCategoryPageUserEvent.UpdateName("Food"))

        assertFalse(viewModel.uiState.value.nameError)
    }
}