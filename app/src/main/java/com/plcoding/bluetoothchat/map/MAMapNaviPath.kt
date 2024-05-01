package com.plcoding.bluetoothchat.map

import com.amap.api.navi.model.AMapNaviPath
import com.amap.api.navi.view.RouteOverLay

data class MAMapNaviPath(var path: AMapNaviPath, var overLay: RouteOverLay) {
}