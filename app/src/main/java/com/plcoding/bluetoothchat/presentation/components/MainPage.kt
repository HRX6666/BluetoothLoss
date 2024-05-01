package com.plcoding.bluetoothchat.presentation.components



import android.content.Context
import android.graphics.BitmapFactory
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.plcoding.bluetoothchat.R
import com.plcoding.bluetoothchat.domain.chat.BluetoothDevice
import com.plcoding.bluetoothchat.map.Map
import com.plcoding.bluetoothchat.presentation.BluetoothUiState
import com.plcoding.bluetoothchat.ui.theme.BluetoothChatTheme


@Composable
fun MainPage(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onSendMessage: (String) -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit//当设备被点击的时候
) {
    var currentTab by remember { mutableStateOf(MainTab.DEVICE) }
    LaunchedEffect(Unit) {
        onStartScan
        onDeviceClick
    }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigation(
                    backgroundColor = Color(0xFF6FD2FF), // 设置底部标题栏的背景色
                    contentColor = Color(0xFF020202) // 设置选中项目的颜色
                )
                {
                    MainTab.values().forEach { tab ->
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    painterResource(tab.iconResId),
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = { Text(tab.title, fontSize = 12.sp) },
                            selected = currentTab == tab,
                            onClick = { currentTab = tab }
                        )

                    }
                }

            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentTab) {
                MainTab.DEVICE -> DevicePage("HC-05",onSendMessage)
                MainTab.LOCATION -> LocationPage()
                MainTab.SETTINGS -> SettingsPage()
            }
        }
    }
}

@Composable
fun DevicePage(deviceName: String,onSendMessage: (String) -> Unit) {
    //, onSettingsClick: () -> Unit, onAlarmClick: () -> Unit


    Box(
        modifier = Modifier
            .fillMaxSize() // 填充整个父容器
            .background(Color.LightGray) // 设置背景色为灰色
    ){
        Surface(
            modifier = Modifier
                .padding(top = 30.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp), // 设置圆角为8dp
            color = Color.White// 透明背景
        ){

            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_bluetooth),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = deviceName,
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Column {
                    Button(
                        onClick ={
                            onSendMessage("Beep") //发送beep信号
                        },
                        modifier = Modifier.padding(top = 30.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFB9E3FF)
                        )
                    ) {
                        Text(text = "点击报警")
                    }
                }
            }
        }

    }
}



@Composable
fun LocationPage() {
    BluetoothChatTheme{
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Map()
        }
    }

}







@Composable
fun SettingsPage() {
    Text(text = "Settings Page", fontSize = 24.sp)
}

enum class MainTab(val iconResId: Int, val title: String) {
    DEVICE(R.drawable.ic_device, "设备"),
    LOCATION(R.drawable.ic_location, "定位"),
    SETTINGS(R.drawable.ic_settings, "设置")
}

@Preview
@Composable
fun MainPagePreview() {
    //  MainPage()
}
