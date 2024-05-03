package com.plcoding.bluetoothchat.presentation.components



import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.bluetoothchat.R
import com.plcoding.bluetoothchat.domain.chat.BluetoothDevice
import com.plcoding.bluetoothchat.map.Map
import com.plcoding.bluetoothchat.presentation.BluetoothUiState
import com.plcoding.bluetoothchat.time.TimePickerDialog
import com.plcoding.bluetoothchat.time.TimePickerDialogContentDescription
import com.plcoding.bluetoothchat.ui.theme.BluetoothChatTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.util.Date

@Composable
fun MainPage(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onSendMessage: (String) -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit,//当设备被点击的时候
    rssi:String
) {
    val currentTimeMillis = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
    val rssi1: Int = rssi.toIntOrNull() ?: 0
    val comparisonResult = rssi1.compareTo(70)
    var currentTab by remember { mutableStateOf(MainTab.DEVICE) }
    val lossAlertDialog= remember { mutableStateOf(false) }//选择技能的弹出框状态

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // 延迟10秒
            if (comparisonResult > 0) {
                // rssi大于70
                onSendMessage("beep")
                lossAlertDialog.value=true
            }
        }
    }
    val formattedDate = dateFormat.format(Date(currentTimeMillis))
    if(lossAlertDialog.value){
        AlertDialog(
            onDismissRequest = { lossAlertDialog.value = false },
            title = {
                    Text(text = formattedDate+"\nHC-05已断开")
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_bluetooth), // 替换为你的图片资源
                        contentDescription = "Your Image",
                        modifier = Modifier.size(200.dp) // 调整图片大小
                    )

                }
            },
            buttons = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { lossAlertDialog.value = false },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF6FD2FF), // 按钮的背景颜色
                            contentColor = Color.White // 按钮中文本的颜色
                        )) {
                        Text(text = "好的")
                    }
                }
            }
        )


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
                MainTab.DEVICE -> DevicePage("HC-05",onSendMessage, rssi = rssi)
                MainTab.LOCATION -> LocationPage( rssi = rssi,onSendMessage)
                MainTab.SETTINGS -> SettingsPage( rssi = rssi,onSendMessage)
            }
        }
    }
}

@Composable
fun DevicePage(deviceName: String,onSendMessage: (String) -> Unit,rssi:String) {

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
                            onSendMessage("beep") //发送beep信号
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
fun LocationPage(rssi:String,onSendMessage: (String) -> Unit) {

    BluetoothChatTheme{
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Map(rssi)
            //val mapInfoList = bluetoothDatabase.getMapInfo()
            //for (mapInfo in mapInfoList) {
            //    val address = mapInfo.first
            //    val timestamp = mapInfo.second
            //    // 处理地图地址信息和时间信息
            //}
        }
    }

}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(rssi:String,onSendMessage: (String) -> Unit) {
    val timePickerState =
        rememberTimePickerState(
            is24Hour = true,
            initialHour = 12,
            initialMinute = 0
        )
    var isShowTimePicker by remember { mutableStateOf(false) }
    var isShowTimePicker2 by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(LocalTime.MIDNIGHT) }
    var endTime by remember { mutableStateOf(LocalTime.MIDNIGHT)}
    // 计算时间差
    val duration = remember {
        derivedStateOf {
            Duration.between(startTime, endTime)
        }
    }

// 获取时间差的分钟数
    val minutesDifference = duration.value.toMinutes()
    BluetoothChatTheme{
        Surface(
            modifier = Modifier
                .padding(top = 30.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp), // 设置圆角为8dp
            color = Color.White// 透明背景
        ) {
            Column {


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
                            painter = painterResource(id = R.drawable.ic_time),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = startTime.toString(),
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Column {
                        Button(
                            onClick = {
                                isShowTimePicker = true
                            },
                            modifier = Modifier.padding(top = 30.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFB9E3FF)
                            )
                        ) {
                            Text(text = "开始时间")
                        }
                    }
                }
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
                            painter = painterResource(id = R.drawable.ic_time),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = endTime.toString(),
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Column {
                        Button(
                            onClick = {
                                isShowTimePicker2 = true
                            },
                            modifier = Modifier.padding(top = 30.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFB9E3FF)
                            )
                        ) {
                            Text(text = "结束时间")
                        }
                    }
                }
                Button(
                    onClick = {
                        onSendMessage("sleep"+minutesDifference.toString())
                    Log.i("time11111",minutesDifference.toString())
                    },
                    modifier = Modifier.padding(top = 30.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFB9E3FF)
                    )
                ) {
                    Text(text = "确定")
                }
            }
        }

        AnimatedVisibility(visible = isShowTimePicker) {
            TimePickerDialog(
                state = timePickerState,
                title = {
                    androidx.compose.material3.Text(text = "选择时间")
                },
                onDismissRequest = { isShowTimePicker = false },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        timePickerState.let {
                            val selectedTime = LocalTime.of(it.hour % 24, it.minute)
                            startTime = selectedTime
                        }
                        isShowTimePicker = false
                    }) {
                        androidx.compose.material3.Text(text = "确定")
                    }
                },
                contentDescription = TimePickerDialogContentDescription(
                    toggleKeyboardButton = "Currently in clock mode, click to switch",
                    toggleScheduleButton = "Currently in keyboard mode, click to switch"
                )
            )
        }


        AnimatedVisibility(visible = isShowTimePicker2) {
            TimePickerDialog(
                state = timePickerState,
                title = {
                    androidx.compose.material3.Text(text = "选择时间")
                },
                onDismissRequest = { isShowTimePicker2 = false },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        timePickerState.let {
                            val selectedTime2 = LocalTime.of(it.hour % 24, it.minute)
                            endTime = selectedTime2
                        }
                        isShowTimePicker2 = false
                    }) {
                        androidx.compose.material3.Text(text = "确定")
                    }
                },
                contentDescription = TimePickerDialogContentDescription(
                    toggleKeyboardButton = "Currently in clock mode, click to switch",
                    toggleScheduleButton = "Currently in keyboard mode, click to switch"
                )
            )
        }





        }

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



