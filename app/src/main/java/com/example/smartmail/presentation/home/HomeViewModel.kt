package com.example.smartmail.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmail.domain.auth.GoogleAuthClient
import com.example.smartmail.domain.repository.MailRepository
import com.example.smartmail.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MailRepository,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    // المتغير الوحيد الذي تراقبه الشاشة
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserData()
        observeMails()
        insertFakeDataForTesting() // 👈 أضفنا هذا السطر
    }

    private fun loadUserData() {
        val user = googleAuthClient.getSignedInUser()
        if (user != null) {
            _uiState.update {
                it.copy(
                    userName = user.username,
                    userPhotoUrl = user.profilePictureUrl
                )
            }
        }
    }

    private fun insertFakeDataForTesting() {
        viewModelScope.launch {
            // Check if the database is empty before inserting
            if (_uiState.value.allMails.isEmpty()) {
                val fakeMails = listOf(
                    com.example.smartmail.data.local.SmartMailEntity(
                        id = "1",
                        senderName = "Sarah (CTO)",
                        senderEmail = "sarah.cto@company.com",
                        subject = "URGENT: Server Architecture Review",
                        fullBody = "Please review the new microservices architecture diagram before the board meeting tomorrow morning.",
                        aiSummary = "Action Required: Review microservices architecture before tomorrow's board meeting.",
                        aiCategory = "Work",
                        isUrgent = true,
                        uiTimeFormatted = "10 mins ago",
                        uiCategoryColor = "#EF4444" // Neon Red
                    ),
                    com.example.smartmail.data.local.SmartMailEntity(
                        id = "2",
                        senderName = "GitHub Actions",
                        senderEmail = "noreply@github.com",
                        subject = "Deployment Successful",
                        fullBody = "Your recent push to the 'main' branch was successfully deployed to production without errors.",
                        aiSummary = "Successful production deployment for 'main' branch.",
                        aiCategory = "Tech",
                        isUrgent = false,
                        uiTimeFormatted = "2 hours ago",
                        uiCategoryColor = "#D946EF" // Cyber Pink
                    ),
                    com.example.smartmail.data.local.SmartMailEntity(
                        id = "3",
                        senderName = "AWS Support",
                        senderEmail = "admin@aws-billing-fake.com",
                        subject = "Action Required: Account Suspended",
                        fullBody = "Click the link below to verify your credit card details or your EC2 instances will be terminated.",
                        aiSummary = "Phishing Alert: Fake AWS suspension notice. Do not click any links.",
                        aiCategory = "Spam",
                        isUrgent = false,
                        uiTimeFormatted = "2 days ago",
                        uiCategoryColor = "#94A3B8" // Slate Gray
                    )
                )
                // Insert into Room Database
                repository.insertMails(fakeMails)
            }
        }
    }

    // الدالة الرئيسية التي تستقبل أي أمر من المستخدم
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.RefreshMails -> {
                // TODO: اطلب من n8n جلب إيميلات جديدة
                // viewModelScope.launch { repository.syncMailsFromN8n() }
            }
            is HomeUiEvent.OnMailClicked -> {
                _uiState.update { it.copy(selectedMail = event.mail) }
            }
            is HomeUiEvent.OnDismissMailDetail -> {
                _uiState.update { it.copy(selectedMail = null) }
            }
            is HomeUiEvent.OnDeleteSwipe -> {
                viewModelScope.launch {
                    // الفكرة 1: Optimistic UI (نحذف من القاعدة فوراً، والشاشة ستتحدث برمشة عين)
                    repository.deleteMail(event.mailId)
                }
            }
            is HomeUiEvent.OnMagicAiButtonClicked -> {
                // سنقوم ببرمجة السحر هنا لاحقاً ✨
            }
        }
    }

    // رادار مراقبة الإيميلات السحري
    private fun observeMails() {
        repository.getSmartMails().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                is Resource.Success -> {
                    val mails = result.data ?: emptyList()
                    // نُشغّل الذكاء الاصطناعي لفرز الإحصائيات للوحة القيادة
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            allMails = mails,
                            urgentMails = mails.filter { it.isUrgent },
                            totalSpamCount = mails.count { it.aiCategory.lowercase() == "spam" },
                            totalWorkCount = mails.count { it.aiCategory.lowercase() == "work" }
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }.launchIn(viewModelScope) // يبقي الرادار يعمل في الخلفية طالما الشاشة مفتوحة
    }
}