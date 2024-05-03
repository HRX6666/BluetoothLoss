package com.plcoding.bluetoothchat.data.chat

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * 监控蓝牙连接状态的变化，并通过回调函数通知调用者
 */
class BluetoothStateReceiver(
    private val onStateChanged: (isConnected: Boolean, BluetoothDevice) -> Unit
): BroadcastReceiver() {
/**
 * 广播接收器接收到广播时调用的方法。它首先从意图中提取蓝牙设备，然后根据意图动作判断蓝牙的连接状态。
 * 如果是 BluetoothDevice.ACTION_ACL_CONNECTED，表示蓝牙ACL连接建立，调用回调函数将连接状态设为 true；
 * 如果是 BluetoothDevice.ACTION_ACL_DISCONNECTED，表示蓝牙ACL连接断开，调用回调函数将连接状态设为 false。
 */
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