
package com.example.smartmail.presentation.home

import com.example.smartmail.data.local.SmartMailEntity

// يمثل حالة الشاشة بأكملها
data class HomeUiState(
    val isLoading: Boolean = true, // هل التطبيق يحمل البيانات؟
    val error: String? = null, // هل يوجد خطأ؟

    // الفكرة 2: لوحة القيادة (إحصائيات ذكية مقسمة جاهزة للواجهة)
    val allMails: List<SmartMailEntity> = emptyList(),
    val urgentMails: List<SmartMailEntity> = emptyList(),
    val totalSpamCount: Int = 0,
    val totalWorkCount: Int = 0,

    val currentContextRule: String = "Available",

    val userName: String? = null,
    val userPhotoUrl: String? = null,

    val selectedMail: SmartMailEntity? = null,
    val selectedCategory: String = "All",

    val isReplying: Boolean = false, // هل نحن في وضع الرد؟
    val replyTone: Float = 0.5f, // نبرة الرد (من 0 إلى 1)
    val generatedReply: String = "", // الرد النهائي المولد
    val isGeneratingReply: Boolean = false
)