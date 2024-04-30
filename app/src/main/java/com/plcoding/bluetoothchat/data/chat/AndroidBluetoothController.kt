package com.plcoding.bluetoothchat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.plcoding.bluetoothchat.domain.chat.BluetoothController
import com.plcoding.bluetoothchat.domain.chat.BluetoothDeviceDomain
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import com.plcoding.bluetoothchat.domain.chat.ConnectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

/**
 * 蓝牙控制器，普通类
 * 实现蓝牙控制器的接口
 */
@SuppressLint("MissingPermission")

class AndroidBluetoothController(
    private val context: Context //获取上下文
): BluetoothController {
    //Ctrl I+Ctrl A,实现接口里的所有方法
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)//调用java的蓝牙管理器
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter//适配器，地址，名称等等
    }

    private var dataTransferService: BluetoothDataTransferService? = null

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())//蓝牙设备列表，设置为一个空列表
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()//下划线扫描设备

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())//连接设备列表
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    /**
     * 广播接收器
     */
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()//得到的新设备是设备到蓝牙设备域
            if(newDevice in devices) devices else devices + newDevice//将设备加入现有的列表之中
        }
    }

    /**
     * 蓝牙状态接收器
     */
    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if(bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device.")
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    override fun startDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return//权限检查
        }

        context.registerReceiver(
            foundDeviceReceiver,//意图过滤器
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return//检查是否有扫描的权限
        }

        bluetoothAdapter?.cancelDiscovery()//取消发现
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {//每次都会返回流
            if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")//如果没有获得许可则抛出异常
            }

            /**
             * 用于连接请求
             */
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )
            /**
             * 用于监听其他设备的连接请求
             */

            var shouldLoop = true
            while(shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()//线程阻塞动作
                } catch(e: IOException) {
                    //中止当前的服务器连接
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)//在一个循环中不断地尝试接收客户端的连接请求，并将连接的客户端赋值给currentClientSocket
                currentClientSocket?.let {
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(it)//设置一个发送数据男的服务
                    dataTransferService = service
                    /**
                     * 只说发出所有监听传入消息的服务
                     */
                    emitAll(
                        service
                            .listenForIncomingMessages()//读取传入的信息
                            .map {
                                ConnectionResult.TransferSucceeded(it)//获得一个蓝牙消息
                            }
                    )
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentClientSocket = bluetoothAdapter
                ?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(//获取BluetoothSocket，允许客户端连接到BluetoothSocket对象
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()//阻止发现新的设备，也许是因为蓝牙聊天，只能和一个人聊天

            currentClientSocket?.let { socket ->//安全实例
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)//阻塞线程直到建立连接
                    /**
                     * 蓝牙数据传输服务
                     */
                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map { ConnectionResult.TransferSucceeded(it) }//将其映射到传输成功的对象并传递蓝牙消息
                        )
                    }
                } catch(e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    /**
     * 尝试发送消息
     */
    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return null
        }

        if(dataTransferService == null) {
            return null
        }
        /**
         * 创建蓝牙信息
         */
        val bluetoothMessage = BluetoothMessage(
            message = message,
           // senderName = bluetoothAdapter?.name ?: "未知名称",
            isFromLocalUser = true,

        )

        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())

        return bluetoothMessage//返回蓝牙信息
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    /**
     * 取消寄存器接收
     */
    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    /**
     * 更新配对设备
     */
    private fun updatePairedDevices() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {//权限检查，若没有许可则返回
            return
        }
        bluetoothAdapter
            ?.bondedDevices//一组蓝牙设备
            ?.map { it.toBluetoothDeviceDomain() }//映射到我们自己的蓝牙设备上，-》BluetoothDeviceMapper
            ?.also { devices ->
                _pairedDevices.update { devices }//updata
            }
    }

    /**
     * 以字符串的形式传递，是否获得权限，一个帮助类函数
     */
    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val SERVICE_UUID = "00001101-0000-1000-8000-00805f9b34fb"
    }
}