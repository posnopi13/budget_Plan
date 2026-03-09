package com.babytime.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baby_status")
data class BabyStatus(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val condition: Int = 3, // 1~5, 5가 최고
    val symptoms: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
