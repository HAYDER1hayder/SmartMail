package com.example.smartmail.data.remote

import com.example.smartmail.data.local.SmartMailEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// هذه الواجهة هي عقد الاتفاق بيننا وبين Spring Boot (الذي صنعه صديقك)
interface SmartMailApi {

    // 1. جلب الإيميلات الجديدة
    @GET("/api/v1/emails/list")
    suspend fun fetchSmartMails(
        @Query("user_id") userId: String
    ): List<SmartMailEntity>

    // 2. بوابة التوثيق (Authentication Gateway)
    // نرسل للسيرفر الكود السري الخاص بجوجل ليسمح لنا بالدخول
    @GET("/api/v1/auth/google")
    suspend fun authenticateWithBackend(
        @Query("code") serverAuthCode: String,
        @Query("uid") firebaseUid: String
    ): Response<String>

}