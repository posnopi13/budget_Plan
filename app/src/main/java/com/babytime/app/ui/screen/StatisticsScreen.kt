package com.babytime.app.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytime.app.data.entity.CategoryType
import com.babytime.app.data.entity.PatternRecord
import com.babytime.app.viewmodel.BabyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(viewModel: BabyViewModel, paddingValues: PaddingValues) {
    val todayRecords by viewModel.todayRecords.collectAsState()
    val weeklyRecords by viewModel.weeklyRecords.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("오늘", "이번 주")

    val records = if (selectedTab == 0) todayRecords else weeklyRecords

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("통계") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = Modifier.padding(paddingValues)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { FeedingStats(records) }
                item { SleepStats(records) }
                item { DiaperStats(records) }
                item { WeeklyBarChart(weeklyRecords) }
            }
        }
    }
}

@Composable
private fun FeedingStats(records: List<PatternRecord>) {
    val formulaRecords = records.filter { it.categoryType == CategoryType.FORMULA }
    val totalMl = formulaRecords.mapNotNull { it.amount }.sum().toInt()
    val avgMl = if (formulaRecords.isNotEmpty()) totalMl / formulaRecords.size else 0

    StatCard(title = "🍼 수유") {
        StatRow("분유 횟수", "${formulaRecords.size}회")
        if (totalMl > 0) StatRow("총 분유량", "${totalMl}ml")
        if (avgMl > 0) StatRow("평균 수유량", "${avgMl}ml")
        val babyFood = records.count { it.categoryType == CategoryType.BABY_FOOD }
        val toddlerFood = records.count { it.categoryType == CategoryType.TODDLER_FOOD }
        if (babyFood > 0) StatRow("이유식", "${babyFood}회")
        if (toddlerFood > 0) StatRow("유아식", "${toddlerFood}회")
    }
}

@Composable
private fun SleepStats(records: List<PatternRecord>) {
    val nightSleep = records.filter { it.categoryType == CategoryType.NIGHT_SLEEP }
    val nap = records.filter { it.categoryType == CategoryType.NAP }
    fun totalMinutes(list: List<PatternRecord>) = list.sumOf {
        if (it.endTime != null) (it.endTime - it.startTime) / 60000L else 0L
    }
    val nightMin = totalMinutes(nightSleep)
    val napMin = totalMinutes(nap)
    val totalMin = nightMin + napMin

    StatCard(title = "😴 수면") {
        StatRow("밤잠", "${nightSleep.size}회 (${nightMin / 60}시간 ${nightMin % 60}분)")
        StatRow("낮잠", "${nap.size}회 (${napMin / 60}시간 ${napMin % 60}분)")
        StatRow("총 수면", "${totalMin / 60}시간 ${totalMin % 60}분")
    }
}

@Composable
private fun DiaperStats(records: List<PatternRecord>) {
    val pee = records.count { it.categoryType == CategoryType.DIAPER_PEE }
    val poop = records.count { it.categoryType == CategoryType.DIAPER_POOP }

    StatCard(title = "🧷 기저귀") {
        StatRow("소변", "${pee}회")
        StatRow("대변", "${poop}회")
        StatRow("합계", "${pee + poop}회")
    }
}

@Composable
private fun WeeklyBarChart(records: List<PatternRecord>) {
    val dayFmt = SimpleDateFormat("E", Locale.KOREAN)
    val groups = (0..6).map { daysAgo ->
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + 86400000L
        val label = dayFmt.format(cal.time)
        val count = records.count { it.startTime in start until end }
        label to count
    }.reversed()

    val maxCount = groups.maxOfOrNull { it.second } ?: 1

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "주간 기록",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            val barColor = MaterialTheme.colorScheme.primary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                groups.forEach { (label, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height((80 * (count.toFloat() / maxCount)).dp)
                        ) {
                            drawRect(
                                color = barColor,
                                topLeft = Offset.Zero,
                                size = Size(size.width, size.height)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall)
                        Text("$count", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
