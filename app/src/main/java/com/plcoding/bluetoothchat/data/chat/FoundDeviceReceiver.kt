package com.plcoding.bluetoothchat.data.chat

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * 广播接收器
 */

class FoundDeviceReceiver(
    private val onDeviceFound: (BluetoothDevice) -> Unit
): BroadcastReceiver() {
    //使用广播接收器的接收功能

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {//检查这个意图动作是否为蓝牙设备
            BluetoothDevice.ACTION_FOUND -> {//如果找到设备
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)//传递一个额外的设备名称
                }
                device?.let(onDeviceFound)
            }
        }
    }
}