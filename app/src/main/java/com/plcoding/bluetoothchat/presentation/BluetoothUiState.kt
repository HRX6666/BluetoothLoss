package com.plcoding.bluetoothchat.presentation

import com.plcoding.bluetoothchat.domain.chat.BluetoothDevice
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage

/**
 * 蓝牙数据类
 */
data class BluetoothUiState(
    // 当前已扫描到的蓝牙设备列表
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    // 当前已配对的蓝牙设备列表
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    // 表示是否已连接到某个蓝牙设备
    val isConnected: Boolean = false,
    // 表示是否正在连接中
    val isConnecting: Boolean = false,
    // 当连接或通信过程中发生错误时的错误信息
    val errorMessage: String? = null,
    // 通信过程中的消息列表
    val messages: List<BluetoothMessage> = emptyList()  // 消息
)
