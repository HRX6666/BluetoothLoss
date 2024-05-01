package com.plcoding.bluetoothchat.map

import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint

data class AMapPoint(var name:String,var position: LatLonPoint){

    fun getLatLon(): LatLng {
        return LatLng(position.latitude,position.longitude)
    }
}