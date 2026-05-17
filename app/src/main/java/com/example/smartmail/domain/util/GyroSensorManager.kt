package com.example.smartmail.domain.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object GyroSensorManager {

    // دالة تقوم بتحويل قراءات الجيروسكوب إلى Flow متدفق من الأرقام (X, Y)
    fun getGyroRotation(context: Context): Flow<Pair<Float, Float>> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (gyroSensor == null) {
            // إذا لم يكن الهاتف يحتوي على جيروسكوب، نعيد قيمة صفرية لكي لا ينهار
            trySend(Pair(0f, 0f))
            close()
            return@callbackFlow
        }

        var currentX = 0f
        var currentY = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    // سرعة الدوران
                    val axisX = event.values[0]
                    val axisY = event.values[1]

                    // تحجيم وتنعيم الحركة لتبدو طبيعية في الـ UI
                    currentX = (currentX + axisY * 0.1f).coerceIn(-1f, 1f)
                    currentY = (currentY + axisX * 0.1f).coerceIn(-1f, 1f)

                    trySend(Pair(currentX, currentY))
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // تسجيل المستشعر
        sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_UI)

        // إيقاف المستشعر عند إغلاق الشاشة لتوفير البطارية
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}