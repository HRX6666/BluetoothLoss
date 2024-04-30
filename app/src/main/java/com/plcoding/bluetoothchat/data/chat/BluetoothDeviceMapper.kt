package com.plcoding.bluetoothchat.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.plcoding.bluetoothchat.domain.chat.BluetoothDeviceDomain

/**
 * 蓝牙消息的映射类
 */
@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,//映射蓝牙名称，需要加入蓝牙连接的权限
        address = address //映射地址
    )
}