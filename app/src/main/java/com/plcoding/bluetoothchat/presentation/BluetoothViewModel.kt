package com.plcoding.bluetoothchat.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.bluetoothchat.domain.chat.BluetoothController
import com.plcoding.bluetoothchat.domain.chat.BluetoothDeviceDomain
import com.plcoding.bluetoothchat.domain.chat.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 采用MVVM的架构，所以这个是MainActivity的ViewModel，主要是一些逻辑函数的部分
 */
@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
): ViewModel() {

    // 存储已配对设备的 RSSI 值映射
    private val _pairedDevicesRssiMap = mutableStateOf<Map<String, Int>>(emptyMap())
    val pairedDevicesRssiMap: State<Map<String, Int>> = _pairedDevicesRssiMap

    // 用于监听蓝牙连接和通信状态的 StateFlow
    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            messages = if(state.isConnected) state.messages else emptyList()//如果连接则有消息，否则为空
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    // 用于管理设备连接的 Job
    private var deviceConnectionJob: Job? = null

    // 初始化，监听蓝牙连接状态和错误信息
    init {
        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update { it.copy(
                errorMessage = error
            ) }
        }.launchIn(viewModelScope)
    }

    // 扩展 Flow<ConnectionResult> 的函数，用于处理连接结果
    fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when(result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update { it.copy(
                        isConnected = true,
                        isConnecting = false,
                        errorMessage = null
                    ) }
                }
                is ConnectionResult.TransferSucceeded -> {
                    _state.update { it.copy(
                        //更新信息传递结果等等.....
                        messages = it.messages + result.message

                    ) }

                }
                is ConnectionResult.Error -> {
                    _state.update { it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.message
                    ) }
                }
            }
        }
            .catch { throwable ->
                bluetoothController.closeConnection()
                _state.update { it.copy(
                    isConnected = false,
                    isConnecting = false,
                ) }
            }
            .launchIn(viewModelScope)
    }


    /**
     * 连接蓝牙
     */
    fun connectToDevice(device: BluetoothDeviceDomain) {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    /**
     * 断开蓝牙的连接
     */
    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update { it.copy(
            isConnecting = false,
            isConnected = false
        ) }
    }

    /**
     * 等待蓝牙的连接
     */
    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    /**
     * 发送消息
     */
    fun sendMessage(message: String) {
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(message)
            if(bluetoothMessage != null) {
                _state.update { it.copy(
                    messages = it.messages + bluetoothMessage
                ) }
            }
        }
    }

    /**
     * 开始扫描蓝牙
     */
    fun startScan() {

        bluetoothController.startDiscovery()

    }

    /**
     * 结束扫描蓝牙
     */
    fun stopScan() {
        bluetoothController.stopDiscovery()
    }
//    suspend fun fromMessage(): String = coroutineScope {
//        val messageChannel = produce<String> {
//            bluetoothController.startBluetoothServer().collect { result ->
//                when (result) {
//                    is ConnectionResult.TransferSucceeded -> {
//                        val bluetoothMessage = result.message
//                        send(bluetoothMessage.toString() + "xxxxx")
//                    }
//                    // 可以根据需要处理其他类型的 ConnectionResult
//                    else -> {
//                        // 其他类型的处理逻辑
//                    }
//                }
//            }
//        }
//
//        messageChannel.receive()
//    }




//清理
    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}