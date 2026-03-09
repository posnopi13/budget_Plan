package com.babytime.app.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.babytime.app.data.entity.CategoryType
import com.babytime.app.data.entity.PatternCategory
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDialog(
    category: PatternCategory,
    onDismiss: () -> Unit,
    onSave: (startTime: Long, endTime: Long?, amount: Float?, unit: String?, subNote: String?, notes: String?) -> Unit
) {
    val now = Calendar.getInstance()
    val startTimeState = rememberTimePickerState(
        initialHour = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour = true
    )
    val endTimeState = rememberTimePickerState(
        initialHour = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour = true
    )
    var amount by remember { mutableStateOf("") }
    var subNote by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val isSleep = category.type == CategoryType.NIGHT_SLEEP || category.type == CategoryType.NAP

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${category.emoji()} ${category.name} 기록") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isSleep) "시작 시간" else "기록 시간",
                    style = MaterialTheme.typography.labelMedium
                )
                TimeInput(state = startTimeState)

                if (isSleep) {
                    Text("종료 시간", style = MaterialTheme.typography.labelMedium)
                    TimeInput(state = endTimeState)
                }

                when (category.type) {
                    CategoryType.FORMULA -> {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("분유량 (ml)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    CategoryType.BABY_FOOD, CategoryType.TODDLER_FOOD -> {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("섭취량 (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    CategoryType.DIAPER_POOP -> {
                        Text("변 상태", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("묽음", "보통", "딱딱함").forEach { opt ->
                                FilterChip(
                                    selected = subNote == opt,
                                    onClick = { subNote = opt },
                                    label = { Text(opt) }
                                )
                            }
                        }
                    }
                    CategoryType.MEDICATION -> {
                        OutlinedTextField(
                            value = subNote,
                            onValueChange = { subNote = it },
                            label = { Text("약 이름") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("용량") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    else -> {} // DIAPER_PEE, CUSTOM* — notes only
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("메모 (선택)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, startTimeState.hour)
                    set(Calendar.MINUTE, startTimeState.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = if (isSleep) Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, endTimeState.hour)
                    set(Calendar.MINUTE, endTimeState.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                } else null
                val unit = when (category.type) {
                    CategoryType.FORMULA -> "ml"
                    CategoryType.BABY_FOOD, CategoryType.TODDLER_FOOD -> "%"
                    CategoryType.MEDICATION -> "ml"
                    else -> null
                }
                onSave(
                    startCal.timeInMillis,
                    endCal?.timeInMillis,
                    amount.toFloatOrNull(),
                    unit,
                    subNote.ifBlank { null },
                    notes.ifBlank { null }
                )
                onDismiss()
            }) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

private fun PatternCategory.emoji(): String = type.emoji
