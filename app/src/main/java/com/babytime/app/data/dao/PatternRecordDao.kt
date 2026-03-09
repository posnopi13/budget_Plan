package com.babytime.app.data.dao

import androidx.room.*
import com.babytime.app.data.entity.PatternRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternRecordDao {
    @Query("SELECT * FROM pattern_records ORDER BY startTime DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 20): Flow<List<PatternRecord>>

    @Query("SELECT * FROM pattern_records WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime DESC")
    fun getRecordsForDay(startOfDay: Long, endOfDay: Long): Flow<List<PatternRecord>>

    @Query("SELECT * FROM pattern_records WHERE startTime >= :startTime ORDER BY startTime DESC")
    fun getRecordsFrom(startTime: Long): Flow<List<PatternRecord>>

    @Insert
    suspend fun insert(record: PatternRecord): Long

    @Update
    suspend fun update(record: PatternRecord)

    @Delete
    suspend fun delete(record: PatternRecord)
}
