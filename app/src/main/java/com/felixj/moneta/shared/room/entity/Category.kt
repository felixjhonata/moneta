package com.felixj.moneta.shared.room.entity

import androidx.annotation.DrawableRes
import androidx.room3.ColumnInfo
import androidx.room3.ColumnInfo.Companion.TEXT
import androidx.room3.ColumnTypeConverters
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.felixj.moneta.shared.room.converter.CategoryIconConverter

@Entity("category")
data class Category(
    @PrimaryKey val id: Int,
    @ColumnInfo("name") val name: String,
    @field:ColumnTypeConverters(CategoryIconConverter::class)
    @ColumnInfo("icon", typeAffinity = TEXT) @DrawableRes val icon: Int
)