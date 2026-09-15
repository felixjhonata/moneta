package com.felixj.moneta.shared.room.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.ActivityType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon

@Dao
interface ActivityDao {
    @Query("""
        SELECT 
            activity.*, 
            category.icon AS category_icon
        FROM activity
        INNER JOIN category ON activity.category_id = category.id
        ORDER BY activity.date DESC
        LIMIT :limit
    """)
    suspend fun getActivities(limit: Int): List<ActivityWithCategoryIcon>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) FROM activity")
    suspend fun getCurrentBalance(): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM activity WHERE type = :type AND date >= :startOfMonth AND date < :startOfNextMonth")
    suspend fun getTotalAmountByTypeAndDateRange(type: ActivityType, startOfMonth: String, startOfNextMonth: String): Long

    @Insert
    suspend fun insertAll(vararg activities: Activity)

    @Update
    suspend fun update(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)
}