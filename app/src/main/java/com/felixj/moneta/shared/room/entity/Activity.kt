package com.felixj.moneta.shared.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "activity",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["category_id"])
    ]
)
data class Activity(
    @PrimaryKey val id: Int,
    @ColumnInfo("category_id") val categoryId: Int,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("date") val date: String,
    @ColumnInfo("amount") val amount: Long,
    @ColumnInfo("notes") val notes: String
)
