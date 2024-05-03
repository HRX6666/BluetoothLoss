package com.plcoding.bluetoothchat.domain.chat

typealias BluetoothDeviceDomain = BluetoothDevice //添加类别名避免冲突

data class BluetoothDevice(
    val name: String?,//蓝牙名称
    val address: String,//蓝牙地址，如AD:.......这样
    val rssi:Int
)