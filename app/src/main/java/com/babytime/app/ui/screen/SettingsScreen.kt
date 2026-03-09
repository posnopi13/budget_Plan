package com.babytime.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytime.app.data.entity.PatternCategory
import com.babytime.app.viewmodel.BabyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: BabyViewModel, paddingValues: PaddingValues) {
    val profile by viewModel.babyProfile.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var nickname by remember(profile) { mutableStateOf(profile?.nickname ?: "") }
    var selectedGender by remember(profile) { mutableStateOf(profile?.gender ?: "UNKNOWN") }
    var birthDate by remember(profile) { mutableLongStateOf(profile?.birthDate ?: 0L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var profileSaved by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = birthDate.takeIf { it > 0 } ?: System.currentTimeMillis()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
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
            // Baby Profile section
            item {
                SectionHeader("👶 아기 정보")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("이름") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("별명") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Birth date picker
                        val birthStr = if (birthDate > 0L)
                            SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN).format(Date(birthDate))
                        else "생년월일 선택"
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(birthStr)
                        }

                        // Gender selector
                        Text("성별", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("MALE" to "남아", "FEMALE" to "여아", "UNKNOWN" to "미정").forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedGender == key,
                                    onClick = { selectedGender = key },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.saveBabyProfile(name, nickname, birthDate, selectedGender)
                                profileSaved = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("저장")
                        }
                        if (profileSaved) {
                            Text(
                                "저장되었습니다!",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Categories management
            item {
                SectionHeader("📋 카테고리 관리")
            }

            items(allCategories) { cat ->
                CategoryManageItem(
                    category = cat,
                    onToggle = { viewModel.toggleCategory(cat) },
                    onDelete = if (!cat.isDefault) ({ viewModel.deleteCategory(cat) }) else null
                )
            }

            item {
                OutlinedButton(
                    onClick = { showAddCategory = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("커스텀 카테고리 추가")
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { birthDate = it }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text("카테고리 추가") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("카테고리 이름") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        viewModel.addCustomCategory(newCategoryName.trim())
                        newCategoryName = ""
                        showAddCategory = false
                    }
                }) { Text("추가") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategory = false }) { Text("취소") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun CategoryManageItem(
    category: PatternCategory,
    onToggle: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(category.colorHex).copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(category.type.emoji)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = category.isActive,
                onCheckedChange = { onToggle() }
            )
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
