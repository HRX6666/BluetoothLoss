package com.plcoding.bluetoothchat.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plcoding.bluetoothchat.map.App.Companion.context
import com.plcoding.bluetoothchat.rssi.BluetoothDatabase

@Composable
fun MapList(mapInfoList: List<Pair<String, String>>) {
    // 只取列表中的第一个元素作为最新数据
    val latestMapInfo = mapInfoList.firstOrNull()

    // 判断是否有数据
    if (latestMapInfo != null) {
        val (address, timestamp) = latestMapInfo
        MapInfoItem(address, timestamp)
    } else {
        // 如果列表为空，显示空白或者提示信息
        Text(text = "数据库为空")
    }
}





@Composable
fun MapInfoItem(address: String, timestamp: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "地址: $address",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "时间：$timestamp",
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMapInfoList() {
    val database = BluetoothDatabase(context) // Initialize your database here
    val mapInfoList = remember { database.getMapInfo() }
    MapList(mapInfoList)
}
