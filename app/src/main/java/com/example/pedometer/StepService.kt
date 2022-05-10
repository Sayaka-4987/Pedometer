package com.example.pedometer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb


object Statics {
    var CumulativeSteps by mutableStateOf(0)    // 开机以来的累计步数
    var TodaySteps by mutableStateOf(0)         // 今日步数
}

// 步数监听器
class StepService: Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var counterSensor: Sensor? = null
    private var steps: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // return super.onStartCommand(intent, flags, startId)
        // 根据 https://developer.android.google.cn/reference/android/hardware/Sensor?hl=zh-cn#TYPE_SIGNIFICANT_MOTION
        // 不能取消这个传感器！

        // 手动 startForeground 能不能保证 5 秒内调用？
        // 通知需要加个 Channel
        val notificationChannel = NotificationChannel("com.example.pedometer", "ChannelForPedometer",
            NotificationManager.IMPORTANCE_MIN)
        // notificationChannel.enableLights(true)
        // notificationChannel.lightColor = Color.White.toArgb()
        // notificationChannel.setShowBadge(true)
        // notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val notification = Notification.Builder(this.applicationContext).setChannelId("com.example.pedometer").build()
        startForeground(startId, notification)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        counterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)    // 得到步数传感器
        sensorManager.registerListener(this, counterSensor, 60000)     // 绑定监听每分钟一次
        return START_STICKY     // 系统资源充裕就自动恢复服务
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            Statics.CumulativeSteps = 0
        } else {
            steps = event.values[0].toInt()
            // 当前传感器数值小于历史数值，说明重新开机过，加到累计步数上
            if (steps < Statics.CumulativeSteps) Statics.CumulativeSteps += steps
            // 否则直接替换当前累计步数
            else Statics.CumulativeSteps = steps
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