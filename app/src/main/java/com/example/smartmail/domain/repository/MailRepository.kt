package com.example.smartmail.domain.repository

import com.example.smartmail.data.local.SmartMailEntity
import com.example.smartmail.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface MailRepository{
    // لاحظ كيف غلفنا قائمة الإيميلات بصندوق الـ Resource الذي صنعناه!
    // هذا يعني أن الـ UI سيعرف متى يظهر علامة التحميل ومتى يظهر البيانات.
    fun getSmartMails(): Flow<Resource<List<SmartMailEntity>>>

    suspend fun insertMails(mails: List<SmartMailEntity>)

    suspend fun syncMailsFromBackend(userId: String) // دالة لجلب الجديد من السيرفر لاحقاً
    
    suspend fun sendCodeToBackend(serverAuthCode: String, uid: String)
    
    suspend fun deleteMail(id: String)
}
