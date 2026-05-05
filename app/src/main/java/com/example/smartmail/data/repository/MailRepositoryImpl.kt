package com.example.smartmail.data.repository

import com.example.smartmail.data.local.SmartMailDao
import com.example.smartmail.data.remote.N8nApi
import com.example.smartmail.domain.repository.MailRepository
import com.example.smartmail.domain.util.Resource
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MailRepositoryImpl @Inject constructor(
    private val dao: SmartMailDao,
    private val api: N8nApi
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

    override suspend fun syncMailsFromN8n(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                // 1. اطلب الإيميلات الجديدة من سيرفر n8n الخاص بصديقك
                // val newMailsFromServer = api.getNewMails(userId)

                // 2. احفظها في قاعدة البيانات المحلية (Room)
                // dao.insertMails(newMailsFromServer)

                // (بما أن السيرفر غير جاهز، وضعناها كتعليق لكي لا ينهار التطبيق،
                // بمجرد أن يعطيك الرابط، امسح علامة التعليق // وسيعمل السحر فوراً!)

                android.util.Log.d("SMART_MAIL_SYNC", "تم الاتصال الوهمي بنجاح! ننتظر رابط n8n.")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override suspend fun deleteMail(id: String) {
        dao.deleteMailById(id)
    }
}