package com.example.smartmail.data.repository

import com.example.smartmail.data.local.SmartMailDao
import com.example.smartmail.data.remote.SmartMailApi
import com.example.smartmail.domain.auth.GoogleAuthClient
import com.example.smartmail.domain.repository.MailRepository
import com.example.smartmail.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MailRepositoryImpl @Inject constructor(
    private val dao: SmartMailDao,
    private val api: SmartMailApi,
    private val authClient: GoogleAuthClient
) : MailRepository {

    // هذه الدالة تقرأ الإيميلات من الهاتف، وتحولها إلى Flow متدفق
    override fun getSmartMails() = dao.getAllMails()
        .map { mails ->
            // إذا نجح الجلب، نضع البيانات في صندوق Success
            Resource.Success(mails) as Resource<List<com.example.smartmail.data.local.SmartMailEntity>>
        }
        .onStart {
            // قبل أن نبدأ، نرسل حالة Loading لتشغيل الأنيميشن في الواجهة
            emit(Resource.Loading())
        }
        .catch { exception ->
            // إذا حدث أي خطأ (Crash)، نلتقطه هنا ونضعه في صندوق Error لمنع انهيار التطبيق
            emit(Resource.Error(message = exception.localizedMessage ?: "حدث خطأ غير متوقع"))
        }


    override suspend fun insertMails(mails: List<com.example.smartmail.data.local.SmartMailEntity>) {
        dao.insertMails(mails)
    }

    override suspend fun syncMailsFromN8n(){
        withContext(Dispatchers.IO) {
            try {
                // 1. جلب التوكن السري من Firebase (بديل الـ userId)
                val token = authClient.getFirebaseIdToken()

                if (token != null) {
                    val authHeader = "Bearer $token"

                    // 2. اطلب الإيميلات الجديدة من سيرفر Spring Boot الخاص بصديقك
                    // val newMailsFromServer = api.fetchSmartMails(token = authHeader)

                    // 3. احفظها في قاعدة البيانات المحلية (Room)
                    // dao.insertMails(newMailsFromServer)

                    android.util.Log.d("API_SYNC", "تم إرسال التوكن بنجاح! ننتظر رابط Spring Boot.")
                } else {
                    android.util.Log.e("API_SYNC", "لا يوجد توكن! يجب تسجيل الدخول أولاً.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    override suspend fun deleteMail(id: String) {
        dao.deleteMailById(id)
    }
}