package com.plcoding.bluetoothchat.presentation.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.bluetoothchat.R
import com.plcoding.bluetoothchat.domain.chat.BluetoothDevice
import com.plcoding.bluetoothchat.presentation.BluetoothUiState
import com.plcoding.bluetoothchat.rssi.BluetoothDatabase

@Composable
fun DeviceScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit,//启动服务器
    onDeviceClick: (BluetoothDevice) -> Unit,//当设备被点击的时候
    rssi:String
) {
    Box{

        Box( modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xC3D5D5D5))){
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                BluetoothDeviceList(
                    pairedDevices = state.pairedDevices,
                    scannedDevices = state.scannedDevices,
                    onClick = onDeviceClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    rssi=rssi
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = { onStartScan() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFAFF6FF)) // 这里设置为红色，您可以根据需要更改颜色
                    ) {
                        Text(text = "开始扫描")
                    }

                    Button(onClick = onStopScan,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFAFF6FF)) ){
                        Text(text = "停止扫描")
                    }
//            Button(onClick = onStartServer) {
//                Text(text = "开启服务端")
//            }
                }
            }
        }

    }

}

@Composable
fun BluetoothDeviceList(
    pairedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    onClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
    rssi:String
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            Text(
                text = "已配对的设备",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(pairedDevices) { device ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(device) }
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = device.name ?: "Unknown",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.body2
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = rssi, // 显示rssi
                    style = MaterialTheme.typography.body2
                )
            }
        }

        item {
            Text(
                text = "未配对的设备",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(scannedDevices) { device ->
            Text(
                text = device.name ?: device.address?:"no",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(device) }
                    .padding(16.dp)
            )
        }
    }
}