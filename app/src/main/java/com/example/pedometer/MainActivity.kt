package com.example.pedometer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.pedometer.ui.theme.PedometerTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        // 申请权限
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1)
        openStepService()   // 开启服务
        super.onCreate(savedInstanceState)

        setContent {
            PedometerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    StepsCountDisplay()
                }
            }
        }
    }

    // 开启计步 Service 的函数
    @RequiresApi(Build.VERSION_CODES.O)
    fun openStepService() {
        val intent = Intent(this, StepService::class.java)    // 绑定 Intent
        startForegroundService(intent)  // 为防止被安卓杀后台，绑定为前台应用
    }
}

@Composable
fun StepsCountDisplay() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "本次开机以来的")
        TextField(value = Statics.CumulativeStepsStr, onValueChange = {Statics.CumulativeStepsStr = it})
    }
}

@Composable
fun MainScreen() {
    Column(modifier = Modifier
        .fillMaxSize()) {
        StepsCountDisplay()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PedometerTheme {
        MainScreen()
    }
}