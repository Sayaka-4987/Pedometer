package com.example.pedometer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Statics {
    var CumulativeStepsStr by mutableStateOf("累积步数: null")
}

// 步数监听器
class StepService: Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var counterSensor: Sensor? = null
    private var steps: Float = 0.0f

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // return super.onStartCommand(intent, flags, startId)
        // 根据 https://developer.android.google.cn/reference/android/hardware/Sensor?hl=zh-cn#TYPE_SIGNIFICANT_MOTION
        // 不能取消这个传感器！
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        counterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)    // 得到步数传感器
        sensorManager.registerListener(this, counterSensor, 60000)     // 绑定监听每分钟一次
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            Statics.CumulativeStepsStr = "累积步数: event = null!"
        } else {
            steps = event.values[0]
            Statics.CumulativeStepsStr = "累积步数: $steps"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}