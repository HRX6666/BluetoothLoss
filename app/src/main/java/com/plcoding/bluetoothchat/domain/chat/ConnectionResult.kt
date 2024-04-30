package com.plcoding.bluetoothchat.domain.chat

/**
 * 定义不同的连接结果
 */
sealed interface ConnectionResult {
    object ConnectionEstablished: ConnectionResult //对象连接结果
    data class TransferSucceeded(val message: BluetoothMessage): ConnectionResult//传递信息，一条蓝牙消息
    data class Error(val message: String): ConnectionResult //错误
}