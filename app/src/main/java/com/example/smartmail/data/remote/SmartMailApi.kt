package com.example.smartmail.data.remote

import com.example.smartmail.data.local.SmartMailEntity
import retrofit2.http.GET
import retrofit2.http.Header

interface SmartMailApi {

    // هنا سنضع رابط صديقك لاحقاً (مثلاً: /api/v1/emails)
    @GET("TODO_YOUR_FRIEND_ENDPOINT_HERE")
    suspend fun fetchSmartMails(
        // 👇 هذا هو مفتاح الأمان الذي يطلبه Spring Boot
        @Header("Authorization") token: String
    ): List<SmartMailEntity>

}