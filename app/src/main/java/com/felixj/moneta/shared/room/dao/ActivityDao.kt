package com.felixj.moneta.shared.room.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.felixj.moneta.shared.room.entity.Activity

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity LIMIT :limit")
    suspend fun getActivities(limit: Int): List<Activity>

    @Insert
    suspend fun insertAll(vararg activities: Activity)

    @Update
    suspend fun update(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)
}