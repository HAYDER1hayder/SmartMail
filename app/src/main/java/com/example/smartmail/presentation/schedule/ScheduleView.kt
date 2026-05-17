package com.example.smartmail.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartmail.data.local.ScheduleEntity
import com.example.smartmail.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleView(
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("My Routine", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = TextPrimary, // زر أبيض كلاسيكي فخم
                contentColor = BackgroundDark,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(paddingValues).padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            // نص بسيط بدون صندوق أزرق
            Text(text = "✨ AI auto-generates replies based on your current time block.", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            if (schedules.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.DateRange, contentDescription = null, tint = CardBorder, modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No routines set yet", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleItemCard(schedule = schedule, onDelete = { viewModel.deleteSchedule(schedule) })
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = BackgroundDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = CardBorder) }
        ) {
            SmartAddSheetContent(
                onSave = { title, start, end, rule ->
                    viewModel.addSchedule(title, start, end, rule)
                    showAddSheet = false
                }
            )
        }
    }
}

// 📌 تصميم بطاقة النشاط النظيفة
@Composable
fun ScheduleItemCard(schedule: ScheduleEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = schedule.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${schedule.startTime} - ${schedule.endTime}", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = schedule.aiReplyRule, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary) }
        }
    }
}

data class RoutinePreset(val title: String, val rule: String)

// 🪄 نافذة الإضافة المنسقة (Bottom Sheet)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAddSheetContent(onSave: (String, String, String, String) -> Unit) {
    var selectedPreset by remember { mutableStateOf<RoutinePreset?>(null) }
    var timeRange by remember { mutableStateOf(8f..15f) }
    var customRule by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    val presets = listOf(
        RoutinePreset("Work", "I'm currently at work. Decline politely or postpone."),
        RoutinePreset("University", "I'm attending classes. Tell them I will reply tonight."),
        RoutinePreset("Sleep", "It's my sleep time. Send an auto-reply that I'll respond tomorrow.")
    )

    fun formatTime(hourFloat: Float): String {
        val hour = hourFloat.toInt()
        val minute = ((hourFloat - hour) * 60).toInt()
        return String.format("%02d:%02d", hour, minute)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Create Routine", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)

        // הקפסולات
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(presets) { preset ->
                val isSelected = selectedPreset == preset
                Surface(
                    onClick = { selectedPreset = preset; customRule = preset.rule; title = preset.title },
                    shape = RoundedCornerShape(100.dp),
                    color = if (isSelected) TextPrimary else CardSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = preset.title,
                        color = if (isSelected) BackgroundDark else TextSecondary,
                        fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = title, onValueChange = { title = it; selectedPreset = null },
            label = { Text("Activity Title", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = TextPrimary, unfocusedBorderColor = CardBorder, focusedContainerColor = CardSurfaceDark, unfocusedContainerColor = CardSurfaceDark),
            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
        )

        // شريط الوقت
        Column(modifier = Modifier.fillMaxWidth().background(CardSurfaceDark, RoundedCornerShape(12.dp)).border(1.dp, CardBorder, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Start", color = TextSecondary, fontSize = 12.sp); Text(formatTime(timeRange.start), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Column(horizontalAlignment = Alignment.End) { Text("End", color = TextSecondary, fontSize = 12.sp); Text(formatTime(timeRange.endInclusive), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            }
            RangeSlider(
                value = timeRange, onValueChange = { timeRange = it }, valueRange = 0f..24f, steps = 47,
                colors = SliderDefaults.colors(thumbColor = TextPrimary, activeTrackColor = TextPrimary, inactiveTrackColor = BackgroundDark)
            )
        }

        OutlinedTextField(
            value = customRule, onValueChange = { customRule = it },
            label = { Text("AI Context Rule", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = TextPrimary, unfocusedBorderColor = CardBorder, focusedContainerColor = CardSurfaceDark, unfocusedContainerColor = CardSurfaceDark),
            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3
        )

        Button(
            onClick = { onSave(title, formatTime(timeRange.start), formatTime(timeRange.endInclusive), customRule) },
            enabled = title.isNotEmpty() && customRule.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TextPrimary, disabledContainerColor = CardSurfaceDark)
        ) {
            Text("Save Routine", color = BackgroundDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}