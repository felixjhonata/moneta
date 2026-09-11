package com.felixj.moneta.shared.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

enum class ActivityType {
    INCOME, EXPENSE
}

@Entity("activity")
data class Activity(
    @PrimaryKey val id: Int,
    @ColumnInfo("category_id") val categoryId: Int,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("date") val date: String,
    @ColumnInfo("amount") val amount: Long,
    @ColumnInfo("type") val type: ActivityType,
    @ColumnInfo("notes") val notes: String
)
