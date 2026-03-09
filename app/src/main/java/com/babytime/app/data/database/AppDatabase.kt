package com.babytime.app.data.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.babytime.app.data.dao.*
import com.babytime.app.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BabyProfile::class, PatternCategory::class, PatternRecord::class, BabyStatus::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun babyProfileDao(): BabyProfileDao
    abstract fun patternCategoryDao(): PatternCategoryDao
    abstract fun patternRecordDao(): PatternRecordDao
    abstract fun babyStatusDao(): BabyStatusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "babytime_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultCategories(database.patternCategoryDao())
                    }
                }
            }

            private suspend fun populateDefaultCategories(dao: PatternCategoryDao) {
                val defaults = listOf(
                    PatternCategory(name = "분유", type = CategoryType.FORMULA, colorHex = "#4FC3F7", displayOrder = 0),
                    PatternCategory(name = "이유식", type = CategoryType.BABY_FOOD, colorHex = "#81C784", displayOrder = 1),
                    PatternCategory(name = "유아식", type = CategoryType.TODDLER_FOOD, colorHex = "#AED581", displayOrder = 2),
                    PatternCategory(name = "밤잠", type = CategoryType.NIGHT_SLEEP, colorHex = "#7986CB", displayOrder = 3),
                    PatternCategory(name = "낮잠", type = CategoryType.NAP, colorHex = "#9575CD", displayOrder = 4),
                    PatternCategory(name = "소변", type = CategoryType.DIAPER_PEE, colorHex = "#FFD54F", displayOrder = 5),
                    PatternCategory(name = "대변", type = CategoryType.DIAPER_POOP, colorHex = "#A1887F", displayOrder = 6),
                    PatternCategory(name = "투약", type = CategoryType.MEDICATION, colorHex = "#EF5350", displayOrder = 7),
                    PatternCategory(name = "기록A", type = CategoryType.CUSTOM_A, colorHex = "#FF8A65", displayOrder = 8),
                    PatternCategory(name = "기록B", type = CategoryType.CUSTOM_B, colorHex = "#F06292", displayOrder = 9),
                )
                dao.insertAll(defaults)
            }
        }
    }
}
