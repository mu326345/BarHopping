package com.yuyu.barhopping.map.sheet

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.yuyu.barhopping.data.Point
import com.yuyu.barhopping.data.PointImages
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class BottomSheetViewModel(val repository: FirebaseRepository) : ViewModel() {

    private val _routeDataList = MutableLiveData<RouteStore>()
    val routeDataList: LiveData<RouteStore>
        get() = _routeDataList


    private val _marketDetailData = MutableLiveData<List<Point>>()
    val marketDetailData: LiveData<List<Point>>
        get() = _marketDetailData

    // user search for point market name
    val pointsList = Transformations.map(routeDataList) {
        it.points
    }

    val marketName = Transformations.map(marketDetailData) {
        it.map { it.name }
    }

    private val _imagesLiveData = MutableLiveData<List<PointImages>>()
    val imagesLiveData: LiveData<List<PointImages>>
        get() = _imagesLiveData

    private val _finishedGame = MutableLiveData<Boolean>()
    val finishedGame: LiveData<Boolean>
        get() = _finishedGame

    val imageUrlAndLatLngLiveData: MediatorLiveData<Pair<List<PointImages>?, List<Point>?>>
     = MediatorLiveData()

    var userId = "yuyu11111"

    /**
     * path routeId then get detail
     * and transformation get market point id list
     */
    fun getRouteDetail(routeId: String) {
        repository.getRouteDetail(object : FirebaseDataSource.RouteCallBack {
            override fun onResult(list: RouteStore) {
                _routeDataList.value = list
            }
        }, routeId)
    }

    /**
     * snap route-images all that for check user finish this route or not
     */
    fun snapUserRouteImages(routeId: String) {
        repository.getUserRouteImages(object : FirebaseDataSource.UserRouteImagesCallBack {
            override fun onResult(imageList: List<PointImages>) {
                _imagesLiveData.value = imageList
                checkUserFinished()
            }
        }, routeId)
    }

    /**
     * use mediatorLiveData put two LiveData together
     */
    fun addUrlAndLatLngToPair() {
        imageUrlAndLatLngLiveData.addSource(_marketDetailData) {
            imageUrlAndLatLngLiveData.value = Pair(_imagesLiveData.value, it)
        }
        imageUrlAndLatLngLiveData.addSource(_imagesLiveData) {
            imageUrlAndLatLngLiveData.value = Pair(it, _marketDetailData.value)
        }
    }

    fun getPointDetailList(points: List<String>) {
        repository.getPointDetailList(object : FirebaseDataSource.PointCallBack {
            override fun onResult(list: List<Point>) {
                _marketDetailData.value = list
            }
        }, points)
    }

    fun checkUserFinished() {
        var count = 0
        imagesLiveData.value?.forEach {
            if (it.userId == userId) {
                count++
            }
        }

        pointsList.value?.size.let {
            if (count == it) {
                Log.v("QAQ", it.toString())
                _finishedGame.value = true
            }
        }
    }

    fun finishedGameToNull() {
        _finishedGame.value = null
    }

    companion object {
        val TAG = "BottomSheetViewModel"
    }
}