package com.felixj.moneta.shared.room.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity("category")
data class Category(
    @PrimaryKey val id: Int,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("icon") val icon: String
)