package com.example.smartmail.data.remote

import com.example.smartmail.data.local.SmartMailEntity
import retrofit2.http.GET
import retrofit2.http.Query
import  retrofit2.Response

interface BackendApi {

    @GET("/api/v1/emails/list") // 👈 رابط افتراضي مؤقت
    suspend fun getNewMails(
        @Query("user_id") userId: String // نرسل رقمك التعريفي للسيرفر ليجلب إيميلاتك أنت فقط
    ): List<SmartMailEntity>

    // تم التعديل ليتوافق مع الـ Controller: @GetMapping("/google") @RequestParam("code")
    @GET("api/v1/auth/google")
    suspend fun authenticateWithBackend (
        @Query("code") serverAuthCode: String,
        @Query("uid") firebaseUid: String
    ): Response<String>

}
