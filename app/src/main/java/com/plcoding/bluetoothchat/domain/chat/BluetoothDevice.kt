package com.plcoding.bluetoothchat.domain.chat

typealias BluetoothDeviceDomain = BluetoothDevice //添加类别名避免冲突

/**
 * 蓝牙扫描到的信息类
 */
data class BluetoothDevice(
    val name: String?,//蓝牙名称
    var address: String="E8:6B:EA:DE:E9:EE",//蓝牙地址，如AD:.......这样
    val rssi:Int
)