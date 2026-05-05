package com.example.smartmail.presentation.home

import com.example.smartmail.data.local.SmartMailEntity

// كل حركة يقوم بها المستخدم يجب أن تُسجل هنا
sealed class HomeUiEvent {
    object RefreshMails : HomeUiEvent() // سحب الشاشة لتحديث الإيميلات
    data class OnMailClicked(val mail: SmartMailEntity) : HomeUiEvent() // الضغط على إيميل لفتحه
    object OnDismissMailDetail : HomeUiEvent() // إغلاق البطاقة/الستوري
    data class OnDeleteSwipe(val mailId: String) : HomeUiEvent() // سحب الإيميل لحذفه
    object OnMagicAiButtonClicked : HomeUiEvent() // الزر السحري لقراءة الخلاصة فقط
}