package com.example.smartmail.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartMailDao {

    // جلب كل الإيميلات (مراقبة حية بفضل Flow)
    @Query("SELECT * FROM smart_mails")
    fun getAllMails(): Flow<List<SmartMailEntity>>

    // حفظ إيميلات جديدة (إذا كان الإيميل موجوداً، قم بتحديثه)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMails(mails: List<SmartMailEntity>)

    // حذف إيميل معين (استعداداً لميزة السحب للحذف - Swipe to delete)
    @Query("DELETE FROM smart_mails WHERE id = :mailId")
    suspend fun deleteMailById(mailId: String)

    // مسح الصندوق بالكامل
    @Query("DELETE FROM smart_mails")
    suspend fun clearAllMails()
}