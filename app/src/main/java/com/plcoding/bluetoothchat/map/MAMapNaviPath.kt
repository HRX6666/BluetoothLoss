package com.plcoding.bluetoothchat.map

import com.amap.api.navi.model.AMapNaviPath
import com.amap.api.navi.view.RouteOverLay

// 定义一个数据类 MAMapNaviPath，表示高德地图导航路径和覆盖物
data class MAMapNaviPath(var path: AMapNaviPath, var overLay: RouteOverLay) {
}
