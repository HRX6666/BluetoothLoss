package com.plcoding.bluetoothchat.data.chat

import android.bluetooth.BluetoothSocket
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import com.plcoding.bluetoothchat.domain.chat.TransferFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * 对于消息收发的函数功能
 */
class BluetoothDataTransferService(
    private val socket: BluetoothSocket//构建一个私人的蓝牙Socket
) {


    /**
     * 监听传入的消息
     */
    fun listenForIncomingMessages(): Flow<BluetoothMessage> {
        return flow {
            /**
             * 先检查一下是否连接蓝牙
             */
            if(!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)//定义一个字节缓冲区，其实是字节数组
            /**
             * 读取传入的数据
             */
            while(true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)//将字节读入数组
                } catch(e: IOException) {
                    throw TransferFailedException()
                }
                /**
                 * 监听传入消息的功能
                 */
                emit(//释放一些
                    buffer.decodeToString(
                        endIndex = byteCount//将字节解析为字符串
                    ).toBluetoothMessage(
                        isFromLocalUser = false
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * 挂起函数发送消息
     */

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch(e: IOException) {
                e.printStackTrace()
                return@withContext false
            }

            true
        }
    }

}