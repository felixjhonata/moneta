package com.felixj.moneta.shared.repository

import com.felixj.moneta.R
import com.felixj.moneta.shared.room.dao.CategoryDao
import com.felixj.moneta.shared.room.entity.Category
import com.felixj.moneta.shared.room.entity.CategoryType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryRepositoryTest {

    @Test
    fun getCategories_withDefaultLimit_delegatesWithMinusOne() = runBlocking {
        val sampleCategories = listOf(
            Category(1, "Utilities", R.drawable.baseline_lightbulb_24, CategoryType.EXPENSE),
            Category(2, "Food", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE)
        )
        val mockDao = mockk<CategoryDao>()
        coEvery { mockDao.getCategories(-1) } returns sampleCategories
        val repository = CategoryRepository(mockDao)

        val result = repository.getCategories()

        coVerify(exactly = 1) { mockDao.getCategories(-1) }
        assertEquals(sampleCategories, result)
    }

    @Test
    fun getCategories_withCustomLimit_delegatesSpecifiedLimit() = runBlocking {
        val sampleCategories = listOf(
            Category(1, "Utilities", R.drawable.baseline_lightbulb_24, CategoryType.EXPENSE),
            Category(2, "Food", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE),
            Category(3, "Transport", R.drawable.baseline_directions_bus_24, CategoryType.EXPENSE),
            Category(4, "Salary", R.drawable.baseline_account_balance_wallet_24, CategoryType.INCOME)
        )
        val mockDao = mockk<CategoryDao>()
        coEvery { mockDao.getCategories(4) } returns sampleCategories
        val repository = CategoryRepository(mockDao)

        val result = repository.getCategories(4)

        coVerify(exactly = 1) { mockDao.getCategories(4) }
        assertEquals(sampleCategories, result)
    }

    @Test
    fun getNextCategoryId_delegatesToDao() = runBlocking {
        val mockDao = mockk<CategoryDao>()
        coEvery { mockDao.getNextCategoryId() } returns 5
        val repository = CategoryRepository(mockDao)

        val result = repository.getNextCategoryId()

        coVerify(exactly = 1) { mockDao.getNextCategoryId() }
        assertEquals(5, result)
    }

    @Test
    fun insertCategory_delegatesToDao() = runBlocking {
        val category = Category(5, "Food", R.drawable.baseline_fastfood_24, CategoryType.EXPENSE)
        val mockDao = mockk<CategoryDao>()
        coEvery { mockDao.insertAll(category) } returns Unit
        val repository = CategoryRepository(mockDao)

        repository.insertCategory(category)

        coVerify(exactly = 1) { mockDao.insertAll(category) }
    }
}
