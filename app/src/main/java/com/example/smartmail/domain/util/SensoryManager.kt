package com.example.smartmail.domain.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.smartmail.R

class SensoryManager(private val context: Context) {

    // 1. تجهيز محرك الاهتزاز
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // --- دوال الاهتزاز (Haptics) ---

    fun vibrateLightClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }

    fun vibrateSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // اهتزاز مزدوج يدل على النجاح (نبضة قصيرة ثم طويلة)
            val timings = longArrayOf(0, 30, 50, 40)
            val amplitudes = intArrayOf(0, 100, 0, 200)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    fun vibrateDelete() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // اهتزاز ثقيل وقصير يدل على المسح
            vibrator.vibrate(VibrationEffect.createOneShot(50, 255))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    // --- دوال الصوت (Audio) ---
    // (استخدمنا try-catch لكي لا ينهار التطبيق إذا نسيت وضع الملفات في مجلد raw)

    // --- دوال الصوت (Audio) المحصنة ضد الانهيار ---

    fun playSwipeSound() {
        try {
            // نستخدم let لكي لا يعمل الكود إلا إذا كان الـ player تم إنشاؤه بنجاح
            MediaPlayer.create(context, R.raw.swipe)?.let { player ->
                player.setOnCompletionListener {
                    it.release() // تحرير الذاكرة فقط بعد انتهاء الصوت
                }
                player.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playMagicSound() {
        try {
            MediaPlayer.create(context, R.raw.magic)?.let { player ->
                player.setOnCompletionListener {
                    it.release()
                }
                player.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playZenSound() {
        try {
            MediaPlayer.create(context, R.raw.zen)?.let { player ->
                player.setOnCompletionListener {
                    it.release()
                }
                player.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}