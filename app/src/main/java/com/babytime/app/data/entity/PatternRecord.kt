package com.babytime.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pattern_records")
data class PatternRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val categoryName: String,
    val categoryType: CategoryType,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val amount: Float? = null,
    val unit: String? = null,
    val subNote: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
