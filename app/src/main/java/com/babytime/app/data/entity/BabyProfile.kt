package com.babytime.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baby_profile")
data class BabyProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val nickname: String = "",
    val birthDate: Long = 0L,
    val gender: String = "UNKNOWN" // MALE, FEMALE, UNKNOWN
)
