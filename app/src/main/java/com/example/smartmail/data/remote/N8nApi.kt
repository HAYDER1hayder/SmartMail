package com.example.smartmail.data.remote

import com.example.smartmail.data.local.SmartMailEntity
import retrofit2.http.GET
import retrofit2.http.Query

interface N8nApi {

    @GET("webhook/get-smart-mails") // 👈 رابط افتراضي مؤقت
    suspend fun getNewMails(
        @Query("user_id") userId: String // نرسل رقمك التعريفي للسيرفر ليجلب إيميلاتك أنت فقط
    ): List<SmartMailEntity>

}