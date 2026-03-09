package com.babytime.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pattern_categories")
data class PatternCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val colorHex: String = "#6B73FF",
    val isDefault: Boolean = true,
    val isActive: Boolean = true,
    val displayOrder: Int = 0
)
