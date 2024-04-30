package com.plcoding.bluetoothchat.data.chat

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BluetoothStateReceiver(
    private val onStateChanged: (isConnected: Boolean, BluetoothDevice) -> Unit
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(//跟级别列表调用
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
        } else {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        when(intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {//如果蓝牙ACL连接
                onStateChanged(true, device ?: return)//改变其状态为true
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {//如果蓝牙ACL断开
                onStateChanged(false, device ?: return)//改变其状态为false
            }
        }
    }
}