package com.example.smartmail.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmail.domain.auth.GoogleAuthClient
import com.example.smartmail.domain.auth.UserData
import com.example.smartmail.domain.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val googleAuthClient: GoogleAuthClient,
    private val sessionManager: SessionManager
) : ViewModel() {

    // متغير لحفظ بيانات المستخدم لعرضها في الشاشة
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData = _userData.asStateFlow()

    init {
        // جلب البيانات فور فتح صفحة البروفايل
        _userData.value = googleAuthClient.getSignedInUser()
    }

    // دالة تسجيل الخروج (تمسح الذاكرة وتغلق الحساب)
    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            googleAuthClient.signOut() // 1. خروج من جوجل وفايربيس
            sessionManager.saveLoginState(false) // 2. مسح الذاكرة (لكي يفتح على Onboarding المرة القادمة)
            onLogoutSuccess() // 3. إخبار الشاشة بالانتقال
        }
    }
}