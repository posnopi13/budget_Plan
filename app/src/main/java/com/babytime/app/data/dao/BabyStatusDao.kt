package com.babytime.app.data.dao

import androidx.room.*
import com.babytime.app.data.entity.BabyStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyStatusDao {
    @Query("SELECT * FROM baby_status ORDER BY date DESC")
    fun getAllStatus(): Flow<List<BabyStatus>>

    @Query("SELECT * FROM baby_status ORDER BY date DESC LIMIT :limit")
    fun getRecentStatus(limit: Int = 10): Flow<List<BabyStatus>>

    @Insert
    suspend fun insert(status: BabyStatus): Long

    @Update
    suspend fun update(status: BabyStatus)

    @Delete
    suspend fun delete(status: BabyStatus)
}
