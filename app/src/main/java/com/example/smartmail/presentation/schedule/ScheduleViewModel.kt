package com.example.smartmail.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmail.data.local.ScheduleDao
import com.example.smartmail.data.local.ScheduleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val dao: ScheduleDao
) : ViewModel() {

    // قراءة الجدول بأكمله من قاعدة البيانات مباشرة وعرضه كحالة
    val schedules = dao.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // دالة لإضافة نشاط جديد
    // 👇 دالة لإضافة نشاط جديد (بدون الأيقونة)
    fun addSchedule(title: String, startTime: String, endTime: String, aiRule: String) {
        viewModelScope.launch {
            val newSchedule = ScheduleEntity(
                title = title,
                startTime = startTime, // ربط البداية
                endTime = endTime,     // ربط النهاية
                aiReplyRule = aiRule   // ربط القاعدة
            )
            dao.insertSchedule(newSchedule)
        }
    }

    // دالة لحذف نشاط
    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            dao.deleteSchedule(schedule)
        }
    }
}