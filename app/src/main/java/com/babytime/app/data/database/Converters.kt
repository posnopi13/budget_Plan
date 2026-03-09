package com.babytime.app.data.database

import androidx.room.TypeConverter
import com.babytime.app.data.entity.CategoryType

class Converters {
    @TypeConverter
    fun fromCategoryType(type: CategoryType): String = type.name

    @TypeConverter
    fun toCategoryType(name: String): CategoryType = CategoryType.valueOf(name)
}
