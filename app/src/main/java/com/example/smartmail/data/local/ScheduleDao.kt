package com.example.smartmail.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    // جلب كل الجدول الزمني مرتباً حسب وقت البداية
    @Query("SELECT * FROM smart_schedules ORDER BY startTime ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    // إضافة نشاط جديد للجدول
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    // حذف نشاط من الجدول
    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
}