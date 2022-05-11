package com.example.pedometer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.pedometer.ui.theme.*
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val dbHelper = StepDBHelper(this)

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        openStepService()   // 开启服务
        ActivityCompat.requestPermissions (
            this,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION,   // 健身动作
                Manifest.permission.BODY_SENSORS),              // 身体传感器
            1
        )   // 申请权限
        super.onCreate(savedInstanceState)
        setContent {
            PedometerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    MainScreen(dbHelper)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        updateStatistics(dbHelper)  // 更新统计信息
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        insertStepData(dbHelper)    // 写入步数数据
        super.onStop()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        insertStepData(dbHelper)    // 写入步数数据
        dbHelper.close()            // 关闭数据库连接
        super.onDestroy()
    }

    // 开启计步 Service 的函数
    @RequiresApi(Build.VERSION_CODES.O)
    fun openStepService() {
        val intent = Intent(this, StepService::class.java)    // 绑定 Intent
        startForegroundService(intent)  // 为防止被安卓系统杀后台，绑定为前台应用
    }
}

// 主界面步数显示
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StepsCountDisplay(dbHelper: StepDBHelper) {
    Box(
        modifier = Modifier
            .padding(top = dp96)
            .size(320.dp)
            .shadow(elevation = dp12, shape = CircleShape)
            .clip(CircleShape)  // 指定为圆形
            .background(color = White)
            .clickable {
                updateStatistics(dbHelper)  // 点击手动更新统计信息
            },
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

// 主界面热量显示
@Composable
fun CalorieConsumptionDisplay() {
    Box(
        modifier = Modifier
            .padding(start = dp36, end = dp36)
            .size(160.dp)
            .shadow(elevation = dp12, shape = CircleShape)
            .clip(CircleShape)  // 指定为圆形
            .background(color = White09f)
            .clickable { }
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(text = "以慢走为例", color = Color.Black.copy(0.6f))
            Text(text = "运动消耗约为", color = Color.Black.copy(0.6f))
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(text = "${(0.04 * (Statics.CumulativeSteps - Statics.YesterdaySteps)).roundToInt()}kCal",
                    textAlign = TextAlign.Center, fontSize = 24.sp)
            }
        }
    }
}

// 主界面食物显示
@Composable
fun FoodConsumptionDisplay() {
    Box(
        modifier = Modifier
            .padding(start = dp36, end = dp36)
            .size(128.dp)
            .shadow(elevation = dp12, shape = CircleShape)
            .clip(CircleShape)  // 指定为圆形
            .background(color = White08f)
            .clickable { }
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(text = "相当于", color = Color.Black.copy(0.6f))
            Text(text = foodCalculator())
        }
    }
}

// 计算以上食物相当于多少热量，返回字符串
fun foodCalculator(): String {
    // 热量信息由麦当劳官网提供
    val foodList = listOf (
        "炸鸡桶\uD83C\uDF57",
        "金拱门桶\uD83C\uDF54",
        "鸡腿堡\uD83C\uDF54",
        "薯条\uD83C\uDF5F",
        "甜筒\uD83C\uDF66",
        "玉米杯\uD83C\uDF3D",
        "苹果\uD83C\uDF4E"
    )

    val heatList = listOf (
        2400,
        1300,
        500,
        200,
        130,
        60,
        10
    )

    val heat = (0.04 * (Statics.CumulativeSteps - Statics.YesterdaySteps)).roundToInt()
    for (i in heatList.indices) {
        val foodHeat = heatList[i]
        Log.i("com.example.pedometer", "HeatList[i] = ${foodHeat}, Heat = ${heat}, i = $i")
        if (heat < foodHeat) {
            continue
        } else {
            val count = heat / foodHeat
            val foodName = foodList[i]
            return "${count}个" + foodName
        }
    }

    return "0个苹果\uD83C\uDF4F"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(dbHelper: StepDBHelper) {
    Column(modifier = Modifier
        .background(brush = GreenBrush)  // 界面背景
        .fillMaxSize()) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            StepsCountDisplay(dbHelper)
        }
        Spacer(modifier = Modifier.height(dp12))
        Row(modifier = Modifier.align(Alignment.End)) {
            CalorieConsumptionDisplay()
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            FoodConsumptionDisplay()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun updateStatistics(dbHelper: StepDBHelper) {
    queryLatestData(dbHelper)       // 自动更新累积步数
    queryYesterdayData(dbHelper)    // 自动更新今日步数
    showAllData(dbHelper)           // DEBUG: 展示全部历史数据
}

/*
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PedometerTheme {
        MainScreen()
    }
}
*/