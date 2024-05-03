package com.plcoding.bluetoothchat.data.chat

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.plcoding.bluetoothchat.rssi.BluetoothDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 广播接收器
 */

class FoundDeviceReceiver(
    private val context: Context,
    private val onDeviceFound: (BluetoothDevice,Int) -> Unit
): BroadcastReceiver() {
    //使用广播接收器的接收功能
    private val tag = this.javaClass
    private lateinit var bluetoothDatabase: BluetoothDatabase

    init {
        // 初始化 bluetoothDatabase
        bluetoothDatabase = BluetoothDatabase(context)
    }
/**
 * 方法是广播接收器接收到广播时调用的方法。它首先检查接收到的意图动作是否为 BluetoothDevice.ACTION_FOUND，
 * 以确定是否发现了蓝牙设备。如果是，它会从意图中提取设备和RSSI值，并通过协程在IO线程中保存和获取RSSI值，
 * 并在主线程中将设备及其RSSI值传递给 onDeviceFound 回调函数。
 */
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(tag.toString(), "onReceive() intent=${intent?.action}")
        when(intent?.action) {//检查这个意图动作是否为蓝牙设备
            BluetoothDevice.ACTION_FOUND -> {//如果找到设备
                val rssi: Int = (intent.extras?.getShort(BluetoothDevice.EXTRA_RSSI) ?: 0).toInt()
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )


                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)//传递一个额外的设备名称
                }
                Log.i("12345678", rssi.toString())
                device?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        // 在 IO 线程中保存并获取 RSSI 值
                        val savedRssi = saveAndGetRssi(it.address, rssi)
                        Log.i("1111111", savedRssi.toString()+it.address)
                        // 在主线程中回调，将设备和 RSSI 值传递给 onDeviceFound
                        withContext(Dispatchers.Main) {
                            onDeviceFound(it, savedRssi)
                        }
                    }
                }
            }
        }
    }

    /**
     * 一个挂起函数
     * 方法用于保存和获取设备的RSSI值。它在IO线程中执行数据库操作，
     * 并在每次保存后延迟200毫秒以获取最新的RSSI值。在发生异常时，
     * 它会返回错误值或其他默认值，并最终关闭数据库连接
     */
    private suspend fun saveAndGetRssi(address: String, rssi: Int): Int = withContext(Dispatchers.IO) {
        var currentRssi = rssi
        try {
            // 保存 RSSI 值
            while (true){
                bluetoothDatabase.saveRssi(address, currentRssi)
                currentRssi = bluetoothDatabase.getRssi(address)
                delay(200)
            }
            // 获取并返回保存的 RSSI 值
            bluetoothDatabase.getRssi(address)
        } catch (e: Exception) {
            // 处理异常
            e.printStackTrace()
            -1 // 返回错误值或其他默认值
        } finally {
            // 关闭数据库连接
            bluetoothDatabase.close()
        }
    }
}