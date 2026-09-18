package com.felixj.moneta.shared.room.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.felixj.moneta.shared.room.dao.ActivityDao
import com.felixj.moneta.shared.room.dao.CategoryDao
import com.felixj.moneta.shared.room.entity.Activity
import com.felixj.moneta.shared.room.entity.Category

@Database(
    entities = [Activity::class, Category::class],
    version = 3
)
abstract class MonetaDatabase: RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun categoryDao(): CategoryDao
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE category ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
        connection.execSQL("UPDATE category SET type = (SELECT type FROM activity WHERE activity.category_id = category.id LIMIT 1) WHERE EXISTS (SELECT 1 FROM activity WHERE activity.category_id = category.id)")
        connection.execSQL("ALTER TABLE activity DROP COLUMN type")
    }
}
