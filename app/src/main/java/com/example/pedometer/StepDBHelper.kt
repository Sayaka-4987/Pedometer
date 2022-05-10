package com.example.pedometer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.provider.BaseColumns
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.Integer.max
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// 列名
object StepColumn : BaseColumns {
    const val TABLE_NAME = "step"                   // 表名
    const val COLUMN_DATE = "date"                  // 日期，格式为字符串文本
    const val COLUMN_TOTAL_STEPS = "total_steps"    // 累积步数
}

// 日期格式化
@RequiresApi(Build.VERSION_CODES.O)
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

// 建表语句
private const val SQL_CREATE_ENTRIES = "CREATE TABLE ${StepColumn.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${StepColumn.COLUMN_DATE} TEXT," +
            "${StepColumn.COLUMN_TOTAL_STEPS} INTEGER)"

// 删表语句
private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${StepColumn.TABLE_NAME}"

// SQLiteHelper 类
class StepDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Step.db"
    }
}

// 获取当前日期的字符串
@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDate() :String {
    // 使用 Java 的 LocalDateTime 和格式化
    val current = LocalDateTime.now()
    return current.format(formatter).toString()
}

// 获取昨天日期的字符串
@RequiresApi(Build.VERSION_CODES.O)
fun getYesterdayDate(): String {
    val t = Calendar.getInstance().timeInMillis
    val l = t - 24 * 3600 * 1000
    val d = Date(l)
    return formatter.format(
        d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    ).toString()
}

// 插入日期和步数数据
@RequiresApi(Build.VERSION_CODES.O)
fun insertStepData(dbHelper: StepDBHelper) {
    // Gets the data repository in write mode
    val db = dbHelper.writableDatabase
    val values = ContentValues().apply {
        put(StepColumn.COLUMN_DATE, getCurrentDate())
        put(StepColumn.COLUMN_TOTAL_STEPS, Statics.CumulativeSteps)
    }
    // Insert the new row, returning the primary key value of the new row
    val newRowId = db?.insert(StepColumn.TABLE_NAME, null, values)
    Log.e("newRowId = ", "$newRowId")
    Log.e("CumulativeSteps = ", "${Statics.CumulativeSteps}")
}

// 查询上一天的最大步数
@RequiresApi(Build.VERSION_CODES.O)
fun queryYesterdayData(dbHelper: StepDBHelper) {
    val yesterday = getYesterdayDate()

    val db = dbHelper.readableDatabase
    val projection = arrayOf(BaseColumns._ID, StepColumn.COLUMN_TOTAL_STEPS)    // 返回主键和步数
    val selection = "${StepColumn.COLUMN_DATE} = ?"         // 选择日期等于昨天的行
    val selectionArgs = arrayOf(yesterday)
    val order = "${BaseColumns._ID} DESC"    // 按主键排序

    val cursor = db.query(
        StepColumn.TABLE_NAME,   // The table to query
        projection,              // The array of columns to return (pass null to get all)
        selection,               // The columns for the WHERE clause
        selectionArgs,           // The values for the WHERE clause
        null,           // don't group the rows
        null,             // don't filter by row groups
        order
    )

    var yesterdaySteps: Int = 0     // 没查到就当是 0
    with(cursor) {
        while (moveToNext()) {
            yesterdaySteps = getInt(getColumnIndexOrThrow(StepColumn.COLUMN_TOTAL_STEPS))
            break
        }
    }
    cursor.close()
    // 更新昨天的最后一次步数记录
    Statics.YesterdaySteps = yesterdaySteps
    Log.e("yesterdaySteps = ", "$yesterdaySteps")
}

// 重启以后用，查询历史最大步数，加到累积步数数据中
fun queryLatestData(dbHelper: StepDBHelper) {
    val db = dbHelper.readableDatabase
    val projection = arrayOf(BaseColumns._ID, StepColumn.COLUMN_TOTAL_STEPS)    // 返回主键和步数

    val order = "${BaseColumns._ID} DESC"    // 按主键排序

    val cursor = db.query(
        StepColumn.TABLE_NAME,   // The table to query
        projection,              // The array of columns to return (pass null to get all)
        null,          // The columns for the WHERE clause
        null,       // The values for the WHERE clause
        null,           // don't group the rows
        null,             // don't filter by row groups
        order
    )

    var historySteps: Int = 0     // 没查到就当是 0
    with(cursor) {
        while (moveToNext()) {
            historySteps = max(getInt(getColumnIndexOrThrow(StepColumn.COLUMN_TOTAL_STEPS)), historySteps)
        }
    }
    cursor.close()
    // 如果历史记录大于当前传感器数值，说明重启过，今天 + 最后一次步数记录
    if (historySteps > Statics.CumulativeSteps) {
        Statics.CumulativeSteps += historySteps
    }
    Log.e("historySteps = ", "$historySteps")
}

fun showAllData(dbHelper: StepDBHelper) {
    val db = dbHelper.readableDatabase
    val projection = arrayOf(BaseColumns._ID, StepColumn.COLUMN_TOTAL_STEPS)    // 返回主键和步数

    val order = "${BaseColumns._ID} DESC"    // 按主键排序

    val cursor = db.query(
        StepColumn.TABLE_NAME,   // The table to query
        null,              // The array of columns to return (pass null to get all)
        null,          // The columns for the WHERE clause
        null,       // The values for the WHERE clause
        null,           // don't group the rows
        null,             // don't filter by row groups
        order
    )

    var historySteps: Int = 0     // 没查到就当是 0
    with(cursor) {
        while (moveToNext()) {
            val steps = getInt(getColumnIndexOrThrow(StepColumn.COLUMN_TOTAL_STEPS))
            val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
            Log.e("id = $id", "steps = $steps")
        }
    }
    cursor.close()

}