package com.felixj.moneta.shared.room.entity

import androidx.annotation.DrawableRes
import androidx.room3.ColumnInfo
import androidx.room3.ColumnInfo.Companion.TEXT
import androidx.room3.ColumnTypeConverters
import androidx.room3.Embedded
import com.felixj.moneta.shared.room.converter.CategoryIconConverter

data class ActivityWithCategoryIcon(
    @Embedded val activity: Activity,
    @field:ColumnTypeConverters(CategoryIconConverter::class)
    @ColumnInfo("category_icon", typeAffinity = TEXT) @DrawableRes val categoryIcon: Int
)
