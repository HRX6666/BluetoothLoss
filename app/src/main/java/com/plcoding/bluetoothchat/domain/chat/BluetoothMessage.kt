package com.plcoding.bluetoothchat.domain.chat

/**
 * 蓝牙发送数据的数据类
 */
data class BluetoothMessage(
    val message: String, //实际想发送的文本
    val isFromLocalUser: Boolean ,//是否发送信息

)