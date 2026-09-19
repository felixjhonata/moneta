package com.felixj.moneta.shared.room.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.felixj.moneta.shared.room.entity.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category LIMIT :limit")
    suspend fun getCategories(limit: Int): List<Category>

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM category")
    suspend fun getNextCategoryId(): Int

    @Insert
    suspend fun insertAll(vararg categories: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}