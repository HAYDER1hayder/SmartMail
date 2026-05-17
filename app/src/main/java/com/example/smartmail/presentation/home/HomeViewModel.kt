package com.example.smartmail.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmail.data.local.ScheduleDao
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MailRepository,
    private val googleAuthClient: GoogleAuthClient,
    private val scheduleDao: ScheduleDao
) : ViewModel() {

    // المتغير الوحيد الذي تراقبه الشاشة
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserData()
        observeMails()
        insertFakeDataForTesting()
        checkCurrentContext()
    }

    // 👇 الدالة الذكية بعد إصلاح نظام قراءة الوقت
    private fun checkCurrentContext() {
        viewModelScope.launch {
            scheduleDao.getAllSchedules().collect { schedules ->
                if (schedules.isEmpty()) {
                    _uiState.update { it.copy(currentContextRule = "Available (No active schedule)") }
                    return@collect
                }

                // 1. استخراج الوقت الحالي بالساعات والدقائق
                val currentHour = java.time.LocalTime.now().hour
                val currentMinute = java.time.LocalTime.now().minute
                // تحويل الوقت الحالي إلى رقم واحد (إجمالي الدقائق) لسهولة المقارنة
                val currentTotalMinutes = (currentHour * 60) + currentMinute

                var foundActivity: com.example.smartmail.data.local.ScheduleEntity? = null

                // 2. البحث في الجدول
                for (schedule in schedules) {
                    try {
                        // تقسيم وقت البداية (مثلاً "08:30" تصبح 8 و 30)
                        val startParts = schedule.startTime.split(":")
                        val startTotalMinutes = (startParts[0].toInt() * 60) + startParts[1].toInt()

                        // تقسيم وقت النهاية
                        val endParts = schedule.endTime.split(":")
                        val endTotalMinutes = (endParts[0].toInt() * 60) + endParts[1].toInt()

                        // هل الوقت الحالي يقع بين البداية والنهاية؟
                        if (currentTotalMinutes in startTotalMinutes..endTotalMinutes) {
                            foundActivity = schedule
                            break // وجدنا النشاط، نتوقف عن البحث
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // تجاهل إذا كان تنسيق الوقت خاطئاً في أحد الأنشطة
                    }
                }

                // 3. تحديث الواجهة بالنتيجة
                if (foundActivity != null) {
                    _uiState.update {
                        it.copy(currentContextRule = foundActivity.aiReplyRule)
                    }
                } else {
                    _uiState.update {
                        it.copy(currentContextRule = "Available (No active schedule right now)")
                    }
                }
            }
        }
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
                    ),
                    com.example.smartmail.data.local.SmartMailEntity(
                        id = "9",
                        senderName = "AWS Support",
                        senderEmail = "admin@aws-billing-fake.com",
                        subject = "Action Required: Account Suspended",
                        fullBody = "Click the link below to verify your credit card details or your EC2 instances will be terminated.",
                        aiSummary = "Phishing Alert: Fake AWS suspension notice. Do not click any links.",
                        aiCategory = "Spam",
                        isUrgent = false,
                        uiTimeFormatted = "2 days ago",
                        uiCategoryColor = "#94A3B8" // Slate Gray
                    ),
                    com.example.smartmail.data.local.SmartMailEntity(
                        id = "4",
                        senderName = "AWS Support",
                        senderEmail = "admin@aws-billing-fake.com",
                        subject = "Action Required: Account Suspended",
                        fullBody = "Click the link below to verify your credit card details or your EC2 instances will be terminated.",
                        aiSummary = "Phishing Alert: Fake AWS suspension notice. Do not click any links.",
                        aiCategory = "Spam",
                        isUrgent = false,
                        uiTimeFormatted = "2 days ago",
                        uiCategoryColor = "#94A3B8" // Slate Gray
                    ),
                    com.example.smartmail.data.local.SmartMailEntity(
                        id = "5",
                        senderName = "AWS Support",
                        senderEmail = "admin@aws-billing-fake.com",
                        subject = "Action Required: Account Suspended",
                        fullBody = "Click the link below to verify your credit card details or your EC2 instances will be terminated.",
                        aiSummary = "Phishing Alert: Fake AWS suspension notice. Do not click any links.",
                        aiCategory = "Spam",
                        isUrgent = false,
                        uiTimeFormatted = "2 days ago",
                        uiCategoryColor = "#94A3B8" // Slate Gray
                    ),
                    com.example.smartmail.data.local.SmartMailEntity(
                        id = "6",
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
                _uiState.update { it.copy(isLoading = true) }
                viewModelScope.launch {

                    // 👇 هذا السطر سيوقظ الـ Repository ليجلب التوكن ويتصل بالسيرفر
                    repository.syncMailsFromN8n()

                    // ... (يمكنك ترك كود المحاكاة insertFakeData مؤقتاً لتستمتع بالشكل حتى يجهز الرابط) ...

                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            is HomeUiEvent.OnMailClicked -> {
                _uiState.update {
                    it.copy(
                        selectedMail = event.mail,
                        isReplying = false,
                        generatedReply = "",
                        replyTone = 0.5f
                    )
                }
            }
            is HomeUiEvent.OnDismissMailDetail -> {
                _uiState.update {
                    it.copy(
                        selectedMail = null,
                        isReplying = false,
                        generatedReply = ""
                    )
                }
            }
            is HomeUiEvent.OnCategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
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
            // 👇 داخل when (event) أضف هذه الحالات:
            is HomeUiEvent.OnQuickReplyClicked -> {
                _uiState.update { it.copy(isReplying = true, generatedReply = "") }
            }
            is HomeUiEvent.OnCancelReply -> {
                _uiState.update { it.copy(isReplying = false) }
            }
            is HomeUiEvent.OnToneChanged -> {
                _uiState.update { it.copy(isReplying = false, generatedReply = "") }
            }
            is HomeUiEvent.OnGenerateReplyClicked -> {
                _uiState.update { it.copy(isGeneratingReply = true) }
                viewModelScope.launch {
                    // 🪄 محاكاة عمل n8n في توليد الرد (سنربطها لاحقاً)
                    kotlinx.coroutines.delay(1500)

                    val toneName = when {
                        _uiState.value.replyTone < 0.3f -> "Formal & Professional"
                        _uiState.value.replyTone > 0.7f -> "Strict & Direct"
                        else -> "Friendly & Casual"
                    }

                    val fakeAiResponse = "Dear ${_uiState.value.selectedMail?.senderName},\n\nBased on your message regarding '${_uiState.value.selectedMail?.subject}', I would like to express that: ${event.shortText}\n\nBest regards,\n${_uiState.value.userName}"

                    _uiState.update { it.copy(
                        isGeneratingReply = false,
                        generatedReply = "[$toneName Tone Applied]\n$fakeAiResponse"
                    ) }
                }
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