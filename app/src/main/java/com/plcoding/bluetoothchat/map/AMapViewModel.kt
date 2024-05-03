package com.plcoding.bluetoothchat.map


import androidx.lifecycle.ViewModel
import com.amap.api.navi.model.AMapCalcRouteResult
import com.amap.api.navi.view.RouteOverLay
import com.plcoding.bluetoothchat.domain.chat.WisplatAMapNaviListener
import kotlinx.coroutines.flow.MutableStateFlow


/**
 * AMapViewModel 类，用于处理与高德地图相关的视图模型逻辑
 */
class AMapViewModel : ViewModel() {

    // 是否显示路线的可变状态流
    val routeShow = MutableStateFlow(false)

    // 存储导航路径的列表
    var routes = ArrayList<MAMapNaviPath>()

    // AMap 导航监听器
    val mAMapNaviListener = object : WisplatAMapNaviListener() {
        // 当路径计算成功时调用的方法
        override fun onCalculateRouteSuccess(p0: AMapCalcRouteResult?) {
            super.onCalculateRouteSuccess(p0)
            val paths = ArrayList<MAMapNaviPath>()
            // 遍历计算的所有路径
            for (i in 0 until p0!!.routeid.size) {
                val path = aMapNavi.naviPaths.get(p0.routeid.get(i))
                val routeOverLay = RouteOverLay(aMap, path, App.context)
                routeOverLay.showEndMarker(false)
                routeOverLay.isTrafficLine = true
                routeOverLay.addToMap()
                path?.let {
                    // 将路径和覆盖物添加到路径列表中
                    paths.add(MAMapNaviPath(it, routeOverLay))
                }
                // 更新导航路径列表
                routes = paths
                // 更新路线显示状态
                routeShow.value = true
            }
        }
    }
}
