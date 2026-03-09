package com.babytime.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytime.app.data.entity.*
import com.babytime.app.data.repository.BabyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class BabyViewModel(private val repository: BabyRepository) : ViewModel() {

    val babyProfile: StateFlow<BabyProfile?> = repository.getBabyProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeCategories: StateFlow<List<PatternCategory>> = repository.getActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<PatternCategory>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentRecords: StateFlow<List<PatternRecord>> = repository.getRecentRecords(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayRecords: StateFlow<List<PatternRecord>> = repository.getTodayRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStatus: StateFlow<List<BabyStatus>> = repository.getAllStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyRecords: StateFlow<List<PatternRecord>> = repository.getRecordsFrom(
        System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Baby Profile ────────────────────────────────────────────────────────
    fun saveBabyProfile(name: String, nickname: String, birthDate: Long, gender: String) {
        viewModelScope.launch {
            val current = babyProfile.value
            repository.saveBabyProfile(
                BabyProfile(
                    id = current?.id ?: 0,
                    name = name,
                    nickname = nickname,
                    birthDate = birthDate,
                    gender = gender
                )
            )
        }
    }

    // ─── Records ─────────────────────────────────────────────────────────────
    fun addRecord(
        category: PatternCategory,
        startTime: Long,
        endTime: Long? = null,
        amount: Float? = null,
        unit: String? = null,
        subNote: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            repository.insertRecord(
                PatternRecord(
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryType = category.type,
                    startTime = startTime,
                    endTime = endTime,
                    amount = amount,
                    unit = unit,
                    subNote = subNote,
                    notes = notes
                )
            )
        }
    }

    fun deleteRecord(record: PatternRecord) {
        viewModelScope.launch { repository.deleteRecord(record) }
    }

    // ─── Categories ──────────────────────────────────────────────────────────
    fun addCustomCategory(name: String, colorHex: String = "#FF8A65") {
        viewModelScope.launch {
            val maxOrder = allCategories.value.maxOfOrNull { it.displayOrder } ?: 0
            repository.insertCategory(
                PatternCategory(
                    name = name,
                    type = CategoryType.CUSTOM,
                    colorHex = colorHex,
                    isDefault = false,
                    displayOrder = maxOrder + 1
                )
            )
        }
    }

    fun toggleCategory(category: PatternCategory) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(isActive = !category.isActive))
        }
    }

    fun deleteCategory(category: PatternCategory) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    // ─── Status ──────────────────────────────────────────────────────────────
    fun addStatus(condition: Int, symptoms: String, notes: String, date: Long) {
        viewModelScope.launch {
            repository.insertStatus(BabyStatus(date = date, condition = condition, symptoms = symptoms, notes = notes))
        }
    }

    fun deleteStatus(status: BabyStatus) {
        viewModelScope.launch { repository.deleteStatus(status) }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    fun getAgeText(birthDate: Long): String {
        if (birthDate == 0L) return ""
        val diffDays = ((System.currentTimeMillis() - birthDate) / (1000 * 60 * 60 * 24)).toInt()
        return when {
            diffDays < 30 -> "${diffDays}일"
            diffDays < 365 -> "${diffDays / 30}개월 ${diffDays % 30}일"
            else -> "${diffDays / 365}살 ${(diffDays % 365) / 30}개월"
        }
    }

    fun generateSuggestions(): List<String> {
        val today = todayRecords.value
        val week = weeklyRecords.value
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val suggestions = mutableListOf<String>()

        val formulaCount = today.count { it.categoryType == CategoryType.FORMULA }
        if (formulaCount == 0 && hour > 10) {
            suggestions.add("오늘 아직 분유 기록이 없어요. 수유 시간을 확인해보세요!")
        }

        val sleepCount = today.count { it.categoryType == CategoryType.NIGHT_SLEEP || it.categoryType == CategoryType.NAP }
        if (sleepCount == 0 && hour > 14) {
            suggestions.add("낮잠 기록이 없네요. 아이가 충분히 자고 있는지 확인해보세요.")
        }

        val diaperCount = today.count { it.categoryType == CategoryType.DIAPER_PEE || it.categoryType == CategoryType.DIAPER_POOP }
        if (diaperCount < 3 && hour > 12) {
            suggestions.add("오늘 기저귀 교체 횟수가 적어요. 수분 섭취를 확인해보세요.")
        }

        val weeklyFormula = week.filter { it.categoryType == CategoryType.FORMULA }
        if (weeklyFormula.isNotEmpty()) {
            val avg = weeklyFormula.mapNotNull { it.amount }.average()
            if (!avg.isNaN() && avg < 100) {
                suggestions.add("이번 주 평균 분유량이 ${avg.toInt()}ml로 적어보여요. 소아과 상담을 권장드려요.")
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("아이가 잘 자라고 있어요! 오늘도 건강한 하루 보내세요 ☀️")
        }
        return suggestions
    }
}
