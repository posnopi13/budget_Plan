package com.babytime.app.data.repository

import com.babytime.app.data.database.AppDatabase
import com.babytime.app.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class BabyRepository(private val database: AppDatabase) {

    // ─── Baby Profile ───────────────────────────────────────────────────────
    fun getBabyProfile(): Flow<BabyProfile?> = database.babyProfileDao().getBabyProfile()

    suspend fun saveBabyProfile(profile: BabyProfile) =
        database.babyProfileDao().insertOrUpdate(profile)

    // ─── Categories ─────────────────────────────────────────────────────────
    fun getActiveCategories(): Flow<List<PatternCategory>> =
        database.patternCategoryDao().getActiveCategories()

    fun getAllCategories(): Flow<List<PatternCategory>> =
        database.patternCategoryDao().getAllCategories()

    suspend fun insertCategory(category: PatternCategory) =
        database.patternCategoryDao().insert(category)

    suspend fun updateCategory(category: PatternCategory) =
        database.patternCategoryDao().update(category)

    suspend fun deleteCategory(category: PatternCategory) =
        database.patternCategoryDao().delete(category)

    // ─── Pattern Records ────────────────────────────────────────────────────
    fun getRecentRecords(limit: Int = 30): Flow<List<PatternRecord>> =
        database.patternRecordDao().getRecentRecords(limit)

    fun getTodayRecords(): Flow<List<PatternRecord>> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
        return database.patternRecordDao().getRecordsForDay(startOfDay, endOfDay)
    }

    fun getRecordsFrom(startTime: Long): Flow<List<PatternRecord>> =
        database.patternRecordDao().getRecordsFrom(startTime)

    suspend fun insertRecord(record: PatternRecord): Long =
        database.patternRecordDao().insert(record)

    suspend fun deleteRecord(record: PatternRecord) =
        database.patternRecordDao().delete(record)

    // ─── Baby Status ─────────────────────────────────────────────────────────
    fun getAllStatus(): Flow<List<BabyStatus>> = database.babyStatusDao().getAllStatus()

    suspend fun insertStatus(status: BabyStatus): Long = database.babyStatusDao().insert(status)

    suspend fun deleteStatus(status: BabyStatus) = database.babyStatusDao().delete(status)
}
