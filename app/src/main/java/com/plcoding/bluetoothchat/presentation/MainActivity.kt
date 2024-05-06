package com.plcoding.bluetoothchat.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.amap.api.services.core.ServiceSettings
import com.plcoding.bluetoothchat.R
import com.plcoding.bluetoothchat.domain.chat.BluetoothDevice
import com.plcoding.bluetoothchat.presentation.components.MainPage
import com.plcoding.bluetoothchat.rssi.BluetoothDatabase
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest
import dagger.hilt.android.AndroidEntryPoint

/**
 * 所有的界面都是在一个activity中，其中切换都是利用的compose组件进行切换的
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * 数据定义专区
     */
    private lateinit var context: Context
    private lateinit var bluetoothDatabase: BluetoothDatabase
    private lateinit var viewModel: BluetoothViewModel
    private val handler = Handler()
    private var rssi:Int=0

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)
        viewModel.connectToDevice(BluetoothDevice("HC-05","E8:6B:EA:DE:E9:EE",1))
        context = this
      //  startScanTask()
        // 实例化 BluetoothDatabase，并传入 context
        bluetoothDatabase = BluetoothDatabase(context)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )//隐藏标题栏让APP看起来好看一点哈哈哈
        context = applicationContext // 初始化 context
        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /* Not needed */ }
        //定位隐私政策同意
        AMapLocationClient.updatePrivacyShow(applicationContext,true,true);
        AMapLocationClient.updatePrivacyAgree(applicationContext,true);
        //地图隐私政策同意
        MapsInitializer.updatePrivacyShow(applicationContext,true,true);
        MapsInitializer.updatePrivacyAgree(applicationContext,true);
        //搜索隐私政策同意
        ServiceSettings.updatePrivacyShow(applicationContext,true,true);
        ServiceSettings.updatePrivacyAgree(applicationContext,true);
        MapsInitializer.setTerrainEnable(true)
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnableBluetooth = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true
            // 如果有蓝牙权限并且蓝牙未开启，则请求开启蓝牙
            if(canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }
// 请求蓝牙和定位权限
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(//请求多个权限
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        }
        // 检查定位权限并请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1);
            }
        }
        // 请求其他权限
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val REQUEST_CODE = 9527
        val build = PermissionRequest.Builder(this).code(REQUEST_CODE)
            .perms(permissions)
            .build()
        EasyPermissions.requestPermissions(this, build)
        setContent {
            val lossAlertDialog= remember { mutableStateOf(false) }//选择技能的弹出框状态

            MaterialTheme{
                val viewModel = hiltViewModel<BluetoothViewModel>()
                val state by viewModel.state.collectAsState()
                val pairedDevicesRssiMap by viewModel.pairedDevicesRssiMap
                LaunchedEffect(key1 = state.errorMessage) {
                    state.errorMessage?.let { message ->
                        lossAlertDialog.value = true
                        Toast.makeText(
                            applicationContext,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                LaunchedEffect(key1 = state.isConnected) {
                    if(state.isConnected) {
                        Toast.makeText(
                            applicationContext,
                            "你已连接成功",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    MainPage(//加入MainPage组件，传入对应的参数
                        state = state,
                        onStartScan = viewModel::startScan,//点击开始扫描
                        onSendMessage = viewModel::sendMessage,
                        onDeviceClick = viewModel::connectToDevice,//如果被点击item连接
                        rssi = rssi.toString()
                    )
                    when {
                        state.isConnecting -> {
                            startScanTask()
//                            Column(
//                                modifier = Modifier.fillMaxSize(),
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                verticalArrangement = Arrangement.Center
//                            ) {
//                                CircularProgressIndicator()
//                                Text(text = "正在连接，请稍后......")
//                            }

                        }
                        /**
                         * 如果成功连接了
                         */
                        state.isConnected -> {
                        }
                        else -> {
//                            DeviceScreen(
//                                state = state,
//                                onStartScan = viewModel::startScan,//点击开始扫描
//                                onStopScan = viewModel::stopScan,//点击停止扫描
//                                onDeviceClick = viewModel::connectToDevice,//如果被点击item连接
//                                onStartServer = viewModel::waitForIncomingConnections,//点击连接服务端
//                                rssi = rssi.toString()
//                            )

                                // 只有在 `lossAlertDialog1` 为 `true` 且 `lossAlertDialog` 为 `false` 时，显示弹出框


                        }
                    }
                }
            }

            if (lossAlertDialog.value) {
                AlertDialog(
                    onDismissRequest = { lossAlertDialog.value = false },
                    title = {
                        Text(text = "HC-05已断开")
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 在对话框中心显示图片
                            Image(
                                painter = painterResource(R.drawable.ic_bluetooth), // 替换为你的图片资源
                                contentDescription = "Your Image",
                                modifier = Modifier.size(200.dp) // 调整图片大小
                            )
                        }
                    },
                    buttons = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    lossAlertDialog.value = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF6FD2FF),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(text = "好的")
                            }
                        }
                    }
                )
            }

        }
    }
    private fun startScanTask() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                viewModel.sendMessage("xxx")

                handler.postDelayed(this, 1000)

            }
        }, 1000)
    }


}