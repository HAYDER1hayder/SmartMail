package com.example.smartmail.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmail.domain.auth.GoogleAuthClient
import com.example.smartmail.domain.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val googleAuthClient: GoogleAuthClient,
    private val sessionManager: SessionManager
) : ViewModel() {


    fun saveUserSession() {
        viewModelScope.launch {
            sessionManager.saveLoginState(true) // حفظ في الهاتف أن المستخدم موجود
        }
    }



}