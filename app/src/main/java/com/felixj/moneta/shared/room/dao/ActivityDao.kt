package com.felixj.moneta.shared.room.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon

@Dao
interface ActivityDao {
    @Query("""
        SELECT 
            activity.*, 
            category.icon AS category_icon,
            category.type AS category_type
        FROM activity
        INNER JOIN category ON activity.category_id = category.id
        ORDER BY activity.date DESC
        LIMIT :limit
    """)
    suspend fun getActivities(limit: Int): List<ActivityWithCategoryIcon>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN category.type = 'INCOME' THEN activity.amount ELSE -activity.amount END), 0)
        FROM activity
        INNER JOIN category ON activity.category_id = category.id
    """)
    suspend fun getCurrentBalance(): Long

    @Query("""
        SELECT COALESCE(SUM(activity.amount), 0)
        FROM activity
        INNER JOIN category ON activity.category_id = category.id
        WHERE category.type = :type AND activity.date >= :startOfMonth AND activity.date < :startOfNextMonth
    """)
    suspend fun getTotalAmountByTypeAndDateRange(type: CategoryType, startOfMonth: String, startOfNextMonth: String): Long

    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM activity")
    suspend fun getNextActivityId(): Int

    @Insert
    suspend fun insertAll(vararg activities: Activity)

    @Update
    suspend fun update(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)
}
