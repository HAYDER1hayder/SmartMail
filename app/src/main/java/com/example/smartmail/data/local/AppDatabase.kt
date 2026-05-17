package com.example.smartmail.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SmartMailEntity::class, ScheduleEntity::class], // الجداول الموجودة في القاعدة
    version = 5, // إصدار القاعدة
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // ربط الـ Dao بالقاعدة
    abstract val smartMailDao: SmartMailDao
    abstract val scheduleDao: ScheduleDao

}