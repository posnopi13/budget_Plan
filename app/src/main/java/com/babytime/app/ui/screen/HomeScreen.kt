package com.babytime.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytime.app.data.entity.PatternCategory
import com.babytime.app.data.entity.PatternRecord
import com.babytime.app.ui.component.RecordDialog
import com.babytime.app.viewmodel.BabyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: BabyViewModel, paddingValues: PaddingValues) {
    val profile by viewModel.babyProfile.collectAsState()
    val categories by viewModel.activeCategories.collectAsState()
    val todayRecords by viewModel.todayRecords.collectAsState()
    var selectedCategory by remember { mutableStateOf<PatternCategory?>(null) }

    val grouped = categories.groupBy { it.type.section }
    val displayName = profile?.let {
        if (it.nickname.isNotBlank()) it.nickname else it.name
    } ?: "아기"
    val ageText = profile?.let { viewModel.getAgeText(it.birthDate) } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "$displayName 의 하루",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (ageText.isNotBlank()) {
                            Text(
                                text = ageText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = Modifier.padding(paddingValues)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today summary card
            item {
                TodaySummaryCard(todayRecords)
            }

            // Category sections
            grouped.forEach { (section, cats) ->
                item {
                    Text(
                        text = section,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        cats.forEach { cat ->
                            val countToday = todayRecords.count { it.categoryId == cat.id }
                            CategoryButton(
                                category = cat,
                                countToday = countToday,
                                onClick = { selectedCategory = cat }
                            )
                        }
                    }
                }
            }

            // Recent records
            if (todayRecords.isNotEmpty()) {
                item {
                    Text(
                        text = "오늘의 기록",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(todayRecords) { record ->
                    RecordItem(record = record, onDelete = { viewModel.deleteRecord(record) })
                }
            }
        }
    }

    selectedCategory?.let { cat ->
        RecordDialog(
            category = cat,
            onDismiss = { selectedCategory = null },
            onSave = { start, end, amount, unit, subNote, notes ->
                viewModel.addRecord(cat, start, end, amount, unit, subNote, notes)
            }
        )
    }
}

@Composable
private fun TodaySummaryCard(records: List<PatternRecord>) {
    val dateStr = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN).format(Date())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "오늘 기록 ${records.size}건",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CategoryButton(
    category: PatternCategory,
    countToday: Int,
    onClick: () -> Unit
) {
    val bgColor = parseHexColor(category.colorHex)
    Box(
        modifier = Modifier
            .width(90.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = category.type.emoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (countToday > 0) {
                Text(
                    text = "${countToday}회",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun RecordItem(record: PatternRecord, onDelete: () -> Unit) {
    val timeFmt = SimpleDateFormat("HH:mm", Locale.KOREAN)
    val timeStr = timeFmt.format(Date(record.startTime))
    val endStr = record.endTime?.let { " ~ ${timeFmt.format(Date(it))}" } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = record.categoryType.emoji, fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.categoryName}  $timeStr$endStr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                val details = buildList {
                    record.amount?.let { add("${it.toInt()}${record.unit ?: ""}") }
                    record.subNote?.let { add(it) }
                    record.notes?.let { add(it) }
                }.joinToString(" · ")
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

internal fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF6B73FF)
    }
}
