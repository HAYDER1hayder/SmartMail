package com.example.smartmail.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // توليد الرقم تلقائياً
    val title: String,           // اسم النشاط (مثلاً: الجامعة، النادي)
    val startTime: String,       // وقت البداية (مثلاً: "08:00")
    val endTime: String,         // وقت النهاية (مثلاً: "12:00")
    val aiReplyRule: String      // القاعدة للذكاء الاصطناعي (مثلاً: "أنا مشغول بالدراسة، ارفض بلباقة")
)