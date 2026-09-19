package com.felixj.moneta.shared.repository

import com.felixj.moneta.shared.room.dao.CategoryDao
import com.felixj.moneta.shared.room.entity.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(private val categoryDao: CategoryDao) {
    suspend fun getCategories(limit: Int = -1): List<Category> = categoryDao.getCategories(limit)

    suspend fun getNextCategoryId(): Int = categoryDao.getNextCategoryId()

    suspend fun insertCategory(category: Category) = categoryDao.insertAll(category)
}
