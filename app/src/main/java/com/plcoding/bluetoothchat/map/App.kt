package com.plcoding.bluetoothchat.map

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 应用程序类，继承自 Application
 */
class App : Application() {

    companion object {
        // 用于保存应用程序上下文的静态属性
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context // 延迟初始化的上下文对象
    }

    /**
     * 当应用程序创建时调用的方法
     */
    override fun onCreate() {
        super.onCreate()
        // 初始化应用程序上下文
        context = applicationContext
    }
}
