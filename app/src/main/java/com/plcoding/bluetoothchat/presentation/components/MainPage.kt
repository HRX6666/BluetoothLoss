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

/**
 * 主界面的compose组件
 * 里面有三个界面分别为设备、定位、设置界面
 * 设备为查看当前设备以及是否选择报警
 * 定位是查看当前地图位置，这里内置的时候是高德地图
 * 设置设置休眠时间
 */
@Composable
fun MainPage(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onSendMessage: (String) -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit,//当设备被点击的时候
    rssi:String
) {
    // 获取当前系统时间的毫秒数
    val currentTimeMillis = System.currentTimeMillis()

// 日期格式化器，用于将时间戳转换为指定格式的日期字符串
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")

// 将 RSSI 字符串转换为整数值，若无法转换则默认为 0
    val rssi1: Int = rssi.toIntOrNull() ?: 0

// 将 RSSI 值与 70 进行比较，返回比较结果
    val comparisonResult = rssi1.compareTo(70)

// 当前选项卡的状态，可用于控制界面显示不同的内容
    var currentTab by remember { mutableStateOf(MainTab.DEVICE) }

// 是否显示丢失警报对话框的状态
    val lossAlertDialog= remember { mutableStateOf(false) }//选择技能的弹出框状态


    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // 延迟10秒
            if (comparisonResult > 0) {
                // 如果 RSSI 大于 70
                onSendMessage("beep") // 发送 "beep" 消息
                lossAlertDialog.value = true // 显示丢失警报对话框
            }
        }
    }

// 将当前时间戳格式化为指定格式的日期字符串
    val formattedDate = dateFormat.format(Date(currentTimeMillis))

// 如果丢失警报对话框需要显示
    if (lossAlertDialog.value) {
        AlertDialog(
            onDismissRequest = { lossAlertDialog.value = false },
            title = {
                // 在对话框标题中显示格式化后的日期和提示消息
                Text(text = formattedDate + "\nHC-05已断开")
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 在对话框中心显示图片
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
                    // 添加一个按钮用于关闭对话框
                    Button(
                        onClick = { lossAlertDialog.value = false },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF6FD2FF), // 按钮的背景颜色
                            contentColor = Color.White // 按钮中文本的颜色
                        )
                    ) {
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

/**
 * 设备界面的compose组件
 */
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


/**
 * 地图部分的组件
 */
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


/**
 * 设置休眠的compose组件
 */
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



// 定义一个枚举类 MainTab，表示主选项卡，包括图标资源 ID 和标题
//底部导航栏部分数据
enum class MainTab(val iconResId: Int, val title: String) {
    DEVICE(R.drawable.ic_device, "设备"), // 设备选项卡，包括设备图标和标题
    LOCATION(R.drawable.ic_location, "定位"), // 定位选项卡，包括定位图标和标题
    SETTINGS(R.drawable.ic_settings, "设置") // 设置选项卡，包括设置图标和标题
}


@Preview
@Composable
fun MainPagePreview() {
    //  MainPage()
    //在这里可以在AS中预览，一般测试用
}



