package com.felixj.moneta.shared.repository

import com.felixj.moneta.R
import com.felixj.moneta.shared.room.dao.ActivityDao
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityRepositoryTest {

    @Test
    fun getActivities_delegatesToDaoAndReturnsMostRecent() = runTest {
        val sampleActivities = listOf(
            ActivityWithCategoryIcon(
                Activity(2, 4, "Salary Newer", "2026-09-05T00:00:00Z", 5000000, ""),
                R.drawable.baseline_lightbulb_24,
                CategoryType.INCOME
            )
        )
        val mockDao = mockk<ActivityDao>()
        coEvery { mockDao.getActivities(1) } returns sampleActivities
        val repository = ActivityRepository(mockDao)

        val result = repository.getActivities(1)

        coVerify(exactly = 1) { mockDao.getActivities(1) }
        assertEquals(1, result.size)
        assertEquals("Salary Newer", result[0].activity.name)
    }

    @Test
    fun getCurrentBalance_delegatesToDao() = runTest {
        val mockDao = mockk<ActivityDao>()
        coEvery { mockDao.getCurrentBalance() } returns 5700000L
        val repository = ActivityRepository(mockDao)

        val balance = repository.getCurrentBalance()

        coVerify(exactly = 1) { mockDao.getCurrentBalance() }
        assertEquals(5700000L, balance)
    }

    @Test
    fun getActivity_delegatesToDao() = runTest {
        val sampleActivity = ActivityWithCategoryIcon(
            Activity(5, 3, "Electricity Bills", "2026-09-05T14:00:00Z", 1200000, "note"),
            R.drawable.baseline_lightbulb_24,
            CategoryType.EXPENSE
        )
        val mockDao = mockk<ActivityDao>()
        coEvery { mockDao.getActivityById(5) } returns sampleActivity
        val repository = ActivityRepository(mockDao)

        val result = repository.getActivity(5)

        coVerify(exactly = 1) { mockDao.getActivityById(5) }
        assertEquals(5, result?.activity?.id)
        assertEquals("Electricity Bills", result?.activity?.name)
    }

    @Test
    fun getTotalAmountByTypeAndDateRange_delegatesToDao() = runTest {
        val mockDao = mockk<ActivityDao>()
        coEvery {
            mockDao.getTotalAmountByTypeAndDateRange(
                CategoryType.INCOME,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z"
            )
        } returns 5000000L
        val repository = ActivityRepository(mockDao)

        val result = repository.getTotalAmountByTypeAndDateRange(
            CategoryType.INCOME,
            "2026-09-01T00:00:00Z",
            "2026-10-01T00:00:00Z"
        )

        coVerify(exactly = 1) {
            mockDao.getTotalAmountByTypeAndDateRange(
                CategoryType.INCOME,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z"
            )
        }
        assertEquals(5000000L, result)
    }
}
