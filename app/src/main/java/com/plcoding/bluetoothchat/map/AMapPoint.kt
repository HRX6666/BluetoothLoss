package com.plcoding.bluetoothchat.map

import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint

/**
 * AMapPoint 数据类，用于表示高德地图上的点
 * @param name 点的名称
 * @param position 点的位置，经纬度坐标
 */
data class AMapPoint(var name: String, var position: LatLonPoint) {

    /**
     * 获取点的经纬度坐标
     * @return 经纬度坐标的 LatLng 对象
     */
    fun getLatLon(): LatLng {
        return LatLng(position.latitude, position.longitude)
    }
}
