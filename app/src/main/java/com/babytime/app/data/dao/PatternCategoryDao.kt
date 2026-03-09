package com.babytime.app.data.dao

import androidx.room.*
import com.babytime.app.data.entity.PatternCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternCategoryDao {
    @Query("SELECT * FROM pattern_categories WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveCategories(): Flow<List<PatternCategory>>

    @Query("SELECT * FROM pattern_categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<PatternCategory>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: PatternCategory): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<PatternCategory>)

    @Update
    suspend fun update(category: PatternCategory)

    @Delete
    suspend fun delete(category: PatternCategory)

    @Query("SELECT COUNT(*) FROM pattern_categories")
    suspend fun count(): Int
}
