package com.example.smartmail.domain.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences // 👈 هذا هو الاستيراد الصحيح الذي كان يمسحه
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// إنشاء مساحة تخزين صغيرة وسريعة جداً في الهاتف
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

@Singleton
class SessionManager @Inject constructor(

    @ApplicationContext context: Context

) {


    private val dataStore = context.dataStore

    // المفتاح الذي سنحفظ به القيمة
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // دالة لحفظ حالة الدخول (تُستدعى عند نجاح جوجل أو عند تسجيل الخروج)
    suspend fun saveLoginState(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    // دالة لقراءة الحالة (ترجع Flow لكي تراقبها الواجهة باستمرار)
    fun getLoginState(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] ?: false // القيمة الافتراضية هي false (غير مسجل)
        }
    }
}