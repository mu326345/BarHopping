package com.yuyu.barhopping.rank.route.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.yuyu.barhopping.data.NewRouteStore
import com.yuyu.barhopping.data.PointData
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class RouteDetailViewModel(
    private val arguments: NewRouteStore,
    private val repository: FirebaseRepository): ViewModel() {

    private val _routeStore = MutableLiveData<NewRouteStore>().apply {
        value = arguments
    }
    val routeStore: LiveData<NewRouteStore>
        get() = _routeStore

    val paths = routeStore.value?.let {it.paths}

    val pointIds = routeStore.value?.let {it.points}

    private val _currentPointDatas = MutableLiveData<List<PointData>>()
    val currentPointDatas: LiveData<List<PointData>>
        get() = _currentPointDatas


    fun getPathLatlngs(): List<LatLng> {
        val pathLatlngs = mutableListOf<LatLng>()

        paths?.forEach {
            val latLngToDouble = it.split(",")
            val lat = latLngToDouble[0].toDouble()
            val lng = latLngToDouble[1].toDouble()
            pathLatlngs.add(LatLng(lat, lng))
        }
        return pathLatlngs
    }

    fun getPointDetailList() {
        pointIds?.let {
            repository.getPointDetailList(object : FirebaseDataSource.PointCallBack {
                override fun onResult(pointDatas: List<PointData>) {
                    _currentPointDatas.value = pointDatas
                }
            }, it)
        }
    }
}