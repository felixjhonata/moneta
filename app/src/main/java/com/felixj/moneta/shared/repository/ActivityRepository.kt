package com.felixj.moneta.shared.repository

import com.felixj.moneta.shared.room.dao.ActivityDao
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import androidx.paging.PagingSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(private val activityDao: ActivityDao) {
    suspend fun getActivities(limit: Int = -1): List<ActivityWithCategoryIcon> = activityDao.getActivities(limit)

    fun getActivitiesPaged(): PagingSource<Int, ActivityWithCategoryIcon> = activityDao.getActivitiesPaged()

    suspend fun getActivity(id: Int): ActivityWithCategoryIcon? = activityDao.getActivityById(id)

    suspend fun getCurrentBalance(): Long = activityDao.getCurrentBalance()

    suspend fun getTotalAmountByTypeAndDateRange(
        type: CategoryType,
        startOfMonth: String,
        startOfNextMonth: String
    ): Long = activityDao.getTotalAmountByTypeAndDateRange(type, startOfMonth, startOfNextMonth)

    suspend fun getNextActivityId(): Int = activityDao.getNextActivityId()

    suspend fun getActivityCount(): Int = activityDao.getActivityCount()

    suspend fun insertActivity(activity: Activity) = activityDao.insertAll(activity)

    suspend fun insertActivities(activities: List<Activity>) = activityDao.insertAll(*activities.toTypedArray())

    suspend fun updateActivity(activity: Activity) = activityDao.update(activity)

    suspend fun deleteActivity(activity: Activity) = activityDao.delete(activity)

    suspend fun deleteAllActivities() = activityDao.deleteAll()
}
