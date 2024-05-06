package com.plcoding.bluetoothchat.rssi
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * 数据库部分
 * 里面存储蓝牙的地址和rssi信号值
 * 存储rssi<-70的时候地图和丢失时候的时间数据
 */
class BluetoothDatabase(context: Context) : SQLiteOpenHelper(context, "BluetoothDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS BluetoothDevices (address TEXT PRIMARY KEY, rssi INTEGER)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS MapInfo (id INTEGER PRIMARY KEY, address TEXT, timestamp INTEGER)")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // 检查旧版本号，执行相应的数据库升级操作
        if (oldVersion < 2) {
            // 示例：在旧版本号小于2时，添加一个新表
            db?.execSQL("CREATE TABLE IF NOT EXISTS NewTable (_id INTEGER PRIMARY KEY, name TEXT)")
        }

        if (oldVersion < 3) {
            // 示例：在旧版本号小于3时，修改旧表结构，添加一个新列
            db?.execSQL("ALTER TABLE OldTable ADD COLUMN new_column TEXT")
        }

        if (oldVersion < 4) {
            // 示例：在旧版本号小于4时，删除旧表
            db?.execSQL("DROP TABLE IF EXISTS OldTable")
        }

        // 如果需要执行其他版本的升级操作，可以继续添加条件判断和相应的数据库操作
    }

    fun saveMapInfo(address: String, timestamp: String) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                if(address.length>0){
                    put("address", address)
                    put("timestamp", timestamp)
                }

            }
            db.insertWithOnConflict("MapInfo", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }
    @SuppressLint("Range")
    fun getMapInfo(): List<Pair<String, String>> {
        val mapInfoList = mutableListOf<Pair<String, String>>()
        readableDatabase.use { db ->
            val cursor = db.query(
                "MapInfo",
                arrayOf("address", "timestamp"),
                null,
                null,
                null,
                null,
                null
            )
            while (cursor.moveToNext()) {
                val address = cursor.getString(cursor.getColumnIndex("address"))
                val timestamp = cursor.getString(cursor.getColumnIndex("timestamp"))
                val mapInfo = Pair(address, timestamp)
                mapInfoList.add(mapInfo)
            }
            cursor.close()
        }
        return mapInfoList
    }
    fun saveRssi(address: String, rssi: Int) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("address", address)
                put("rssi", rssi)
            }
            db.insertWithOnConflict("BluetoothDevices", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }
    fun clearRssi(address: String) {
        writableDatabase.use { db ->
            val whereClause = "address = ?"
            val whereArgs = arrayOf(address)
            db.delete("BluetoothDevices", whereClause, whereArgs)
        }
    }


    @SuppressLint("Range")
    fun getRssi(address: String): Int {
        readableDatabase.use { db ->
            val cursor = db.query(
                "BluetoothDevices",
                arrayOf("rssi"),
                "address = ?",
                arrayOf(address),
                null,
                null,
                null
            )
            var rssi = 0
            if (cursor.moveToFirst()) {
                rssi = cursor.getInt(cursor.getColumnIndex("rssi"))
            }
            cursor.close()
            return rssi
        }
    }
}

