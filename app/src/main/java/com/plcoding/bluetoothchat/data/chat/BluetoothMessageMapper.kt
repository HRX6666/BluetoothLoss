package com.plcoding.bluetoothchat.data.chat

import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage

/**
 * 一方面将带有消息对象的蓝牙转换为字节数组，通过蓝牙发送
 * 另一方面从字节数组中获得一个字符串，将其转换为蓝牙消息对象
 */

fun String.toBluetoothMessage(isFromLocalUser: Boolean): BluetoothMessage {//将字符串转换为蓝牙消息
    val name = substringBeforeLast("#")//提取发件人的名字
    val message = substringAfter("#")//提取发送的信息
    val end  =substringAfter("#")//帧尾
    return BluetoothMessage(//返回一个蓝牙消息对象
        message = message,
        isFromLocalUser = isFromLocalUser
    )
}
/**
 * 两字节数组
 */
/**
 * 向蓝牙小车发送数据
 * sendername为帧头
 * E为帧尾
 * 使用模块：开火，发动技能，向前，向后，左转，右转，比赛开始，比赛结束
 */
fun BluetoothMessage.toByteArray(): ByteArray {//两字节数组，通过数组返回
    return "${message}".encodeToByteArray()//用标签来区分两个字段，将其编译为一个字节数组，这样做很容易返回一个by数组
}

























