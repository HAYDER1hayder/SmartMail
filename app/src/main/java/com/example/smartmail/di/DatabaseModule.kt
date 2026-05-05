package com.example.smartmail.di

import android.app.Application
import androidx.room.Room
import com.example.smartmail.data.local.AppDatabase
import com.example.smartmail.data.local.SmartMailDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 1. نبني قاعدة البيانات مرة واحدة فقط في حياة التطبيق (Singleton)
    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "smart_mail_db" // اسم قاعدة البيانات في الهاتف
        ).fallbackToDestructiveMigration() // أسلوب احترافي: مسح الداتا القديمة إذا غيرنا شكل الجدول بدلاً من الانهيار
            .build()
    }

    // 2. نوفر الـ Dao (البوابة) لكي نستخدمه في الـ Repository لاحقاً
    @Provides
    @Singleton
    fun provideSmartMailDao(db: AppDatabase): SmartMailDao {
        return db.smartMailDao
    }
}