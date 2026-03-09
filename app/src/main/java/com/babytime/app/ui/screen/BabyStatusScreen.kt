package com.babytime.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytime.app.data.entity.BabyStatus
import com.babytime.app.viewmodel.BabyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BabyStatusScreen(viewModel: BabyViewModel, paddingValues: PaddingValues) {
    val statusList by viewModel.allStatus.collectAsState()
    val suggestions = remember(viewModel.todayRecords, viewModel.weeklyRecords) {
        viewModel.generateSuggestions()
    }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("아기 상태") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "상태 추가")
            }
        },
        modifier = Modifier.padding(paddingValues)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // AI Suggestions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "관리 포인트",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        suggestions.forEach { tip ->
                            Text(
                                text = "• $tip",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (statusList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "아직 상태 기록이 없어요.\n+ 버튼으로 기록을 추가해보세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    Text(
                        "상태 기록",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(statusList) { status ->
                    StatusItem(
                        status = status,
                        onDelete = { viewModel.deleteStatus(status) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        StatusDialog(
            onDismiss = { showDialog = false },
            onSave = { condition, symptoms, notes ->
                viewModel.addStatus(condition, symptoms, notes, System.currentTimeMillis())
            }
        )
    }
}

@Composable
private fun StatusItem(status: BabyStatus, onDelete: () -> Unit) {
    val dateFmt = SimpleDateFormat("M월 d일 HH:mm", Locale.KOREAN)
    val conditionEmoji = listOf("😰", "😟", "😐", "😊", "😄")[status.condition - 1]

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(conditionEmoji, fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dateFmt.format(Date(status.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (status.symptoms.isNotBlank()) {
                    Text(
                        "증상: ${status.symptoms}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (status.notes.isNotBlank()) {
                    Text(
                        status.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
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

@Composable
private fun StatusDialog(
    onDismiss: () -> Unit,
    onSave: (condition: Int, symptoms: String, notes: String) -> Unit
) {
    var condition by remember { mutableIntStateOf(3) }
    var symptoms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val conditionLabels = listOf("많이 힘들어요" to "😰", "조금 안좋아요" to "😟", "보통이에요" to "😐", "좋아요" to "😊", "아주 좋아요" to "😄")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("아기 상태 기록") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("오늘의 컨디션", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    conditionLabels.forEachIndexed { index, (_, emoji) ->
                        val level = index + 1
                        FilterChip(
                            selected = condition == level,
                            onClick = { condition = level },
                            label = { Text(emoji, fontSize = 20.sp) }
                        )
                    }
                }
                Text(
                    conditionLabels[condition - 1].first,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("증상 (열, 기침, 발진 등)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("메모") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(condition, symptoms, notes)
                onDismiss()
            }) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
