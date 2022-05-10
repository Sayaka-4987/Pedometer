package com.example.pedometer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.pedometer.ui.theme.*

class MainActivity : ComponentActivity() {
    private val dbHelper = StepDBHelper(this)

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        openStepService()   // 开启服务
        // 申请权限
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1)
        // showAllData(dbHelper)   // DEBUG: 展示全部历史数据
        super.onCreate(savedInstanceState)
        setContent {
            PedometerTheme {
                queryLatestData(dbHelper)       // 更新累积步数
                queryYesterdayData(dbHelper)    // 更新今日步数
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    MainScreen()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        insertStepData(dbHelper)    // 写入步数数据
        dbHelper.close()
        super.onDestroy()
    }

    // 开启计步 Service 的函数
    @RequiresApi(Build.VERSION_CODES.O)
    fun openStepService() {
        val intent = Intent(this, StepService::class.java)    // 绑定 Intent
        startForegroundService(intent)  // 为防止被安卓系统杀后台，绑定为前台应用
    }
}

@Composable
fun StepsCountDisplay() {
    Box(
        modifier = Modifier
            .padding(start = dp36, end = dp36, top = dp128)
            .size(320.dp)
            .shadow(elevation = dp12, shape = CircleShape)
            .clip(CircleShape)  // 指定为圆形
            .background(color = Color.White)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(240.dp)
                .align(alignment = Alignment.Center) // 居中
        ) {
            // 让步数和文字上下左右都居中
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "今日步数", fontSize = 24.sp)
                    Text(text = "${Statics.CumulativeSteps - Statics.YesterdaySteps}",   // 显示长度上限 5 位数
                        fontSize = 84.sp,
                        textAlign = TextAlign.Center,
                        //modifier = Modifier.align(Alignment.Center),
                        maxLines = 1)
                    Spacer(modifier = Modifier.height(dp8))
                    Text(text = "历史累积步数: ${Statics.CumulativeSteps}",
                        color = Color.Black.copy(0.6f))
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(modifier = Modifier
        .background(brush = GreenBrush)  // 界面背景
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