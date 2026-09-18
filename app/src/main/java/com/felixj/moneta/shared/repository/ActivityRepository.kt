package com.felixj.moneta.shared.repository

import com.felixj.moneta.shared.room.dao.ActivityDao
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(private val activityDao: ActivityDao) {
    suspend fun getActivities(limit: Int = -1): List<ActivityWithCategoryIcon> = activityDao.getActivities(limit)

    suspend fun getCurrentBalance(): Long = activityDao.getCurrentBalance()

    suspend fun getTotalAmountByTypeAndDateRange(
        type: CategoryType,
        startOfMonth: String,
        startOfNextMonth: String
    ): Long = activityDao.getTotalAmountByTypeAndDateRange(type, startOfMonth, startOfNextMonth)
}
