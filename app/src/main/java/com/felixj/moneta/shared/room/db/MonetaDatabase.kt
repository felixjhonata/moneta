package com.felixj.moneta.shared.room.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.felixj.moneta.shared.room.dao.ActivityDao
import com.felixj.moneta.shared.room.dao.CategoryDao
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.Category

@Database(entities = [Activity::class, Category::class], version = 1)
abstract class MonetaDatabase: RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun categoryDao(): CategoryDao
}