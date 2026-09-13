package com.felixj.moneta.shared.repository

import com.felixj.moneta.R
import com.felixj.moneta.shared.room.dao.CategoryDao
import com.felixj.moneta.shared.room.entity.Category
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryRepositoryTest {

    private class FakeCategoryDao(
        private val categories: List<Category> = emptyList()
    ) : CategoryDao {
        var lastLimitPassed: Int? = null

        override suspend fun getCategories(limit: Int): List<Category> {
            lastLimitPassed = limit
            return if (limit < 0) categories else categories.take(limit)
        }

        override suspend fun insertAll(vararg categories: Category) {}
        override suspend fun update(category: Category) {}
        override suspend fun delete(category: Category) {}
    }

    @Test
    fun getCategories_withDefaultLimit_delegatesWithMinusOne() = runBlocking {
        val sampleCategories = listOf(
            Category(1, "Utilities", R.drawable.baseline_lightbulb_24),
            Category(2, "Food", R.drawable.baseline_fastfood_24)
        )
        val fakeDao = FakeCategoryDao(sampleCategories)
        val repository = CategoryRepository(fakeDao)

        val result = repository.getCategories()

        assertEquals(-1, fakeDao.lastLimitPassed)
        assertEquals(sampleCategories, result)
    }

    @Test
    fun getCategories_withCustomLimit_delegatesSpecifiedLimit() = runBlocking {
        val sampleCategories = listOf(
            Category(1, "Utilities", R.drawable.baseline_lightbulb_24),
            Category(2, "Food", R.drawable.baseline_fastfood_24),
            Category(3, "Transport", R.drawable.baseline_directions_bus_24),
            Category(4, "Salary", R.drawable.baseline_account_balance_wallet_24),
            Category(5, "Bonus", R.drawable.baseline_account_balance_wallet_24)
        )
        val fakeDao = FakeCategoryDao(sampleCategories)
        val repository = CategoryRepository(fakeDao)

        val result = repository.getCategories(4)

        assertEquals(4, fakeDao.lastLimitPassed)
        assertEquals(4, result.size)
        assertEquals("Utilities", result[0].name)
        assertEquals("Salary", result[3].name)
    }
}
