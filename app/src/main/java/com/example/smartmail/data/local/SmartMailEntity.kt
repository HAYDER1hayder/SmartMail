package com.example.smartmail.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_mails")
data class SmartMailEntity(
    @PrimaryKey val id: String, // الآي دي القادم من n8n
    val senderName: String,
    val senderEmail: String,
    val subject: String,
    val fullBody: String,
    val aiSummary: String,
    val aiCategory: String,
    val isUrgent: Boolean,
    val uiTimeFormatted: String,
    val uiCategoryColor: String // لون التصنيف (مثلاً #FF0000 أو #00E676)
)
