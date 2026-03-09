package com.babytime.app.data.dao

import androidx.room.*
import com.babytime.app.data.entity.BabyProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyProfileDao {
    @Query("SELECT * FROM baby_profile LIMIT 1")
    fun getBabyProfile(): Flow<BabyProfile?>

    @Query("SELECT * FROM baby_profile LIMIT 1")
    suspend fun getBabyProfileOnce(): BabyProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: BabyProfile)
}
