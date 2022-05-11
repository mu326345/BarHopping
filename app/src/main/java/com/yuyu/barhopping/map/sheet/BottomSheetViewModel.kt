package com.yuyu.barhopping.map.sheet

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.yuyu.barhopping.data.*
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class BottomSheetViewModel(val repository: FirebaseRepository) : ViewModel() {

    private val _usersDataList = MutableLiveData<List<User>>()
    val usersDataList: LiveData<List<User>>
        get() = _usersDataList

    private val _friendsDataList = MutableLiveData<List<FriendsProgress>>()
    val friendsDataList: LiveData<List<FriendsProgress>>
        get() = _friendsDataList

    private val _routeDataList = MutableLiveData<RouteStore>()
    val routeDataList: LiveData<RouteStore>
        get() = _routeDataList

    private val _marketDetailData = MutableLiveData<List<PointData>>()
    val marketDetailData: LiveData<List<PointData>>
        get() = _marketDetailData

    // onRoute points placeId use for search market name
    val pointsIdList = Transformations.map(routeDataList) {
        it.points
    }

    val marketName = Transformations.map(marketDetailData) {
        it.map { it.name }
    }

    private val _marketNameAndStateFromDb = MutableLiveData<List<SheetItem.MarketNameAndState>>()
    val marketNameAndStateFromDb: LiveData<List<SheetItem.MarketNameAndState>>
        get() = _marketNameAndStateFromDb

    private val _marketNameAndState = MutableLiveData<List<SheetItem.MarketNameAndState>>()
    val marketNameAndState: LiveData<List<SheetItem.MarketNameAndState>>
        get() = _marketNameAndState

    private val _routeDetailLatLngList = MutableLiveData<List<LatLng>>()
    val routeDetailLatLngList: LiveData<List<LatLng>>
        get() = _routeDetailLatLngList

    private val _imagesLiveData = MutableLiveData<List<OnRouteUserImages>>()
    val imagesLiveData: LiveData<List<OnRouteUserImages>>
        get() = _imagesLiveData

    private val _usersLocationLiveData = MutableLiveData<List<OnRouteUserPartners>>()
    val usersLocationLiveData: LiveData<List<OnRouteUserPartners>>
        get() = _usersLocationLiveData

    private val _finishedGame = MutableLiveData<Boolean>()
    val finishedGame: LiveData<Boolean>
        get() = _finishedGame

    private val _imageCount = MutableLiveData<Int>()
    val imageCount: LiveData<Int>
        get() = _imageCount

    private val _totalMarket = MutableLiveData<Int>()
    val totalMarket: LiveData<Int>
        get() = _totalMarket

    private val _nextName = MutableLiveData<String>()
    val nextName: LiveData<String>
        get() = _nextName

    val imageImageAndLatLngLiveData: MediatorLiveData<Pair<List<OnRouteUserImages>?, List<PointData>?>>
     = MediatorLiveData()

    val checkUserFinishedLiveData: MediatorLiveData<Pair<List<OnRouteUserImages>?, List<String>?>>
            = MediatorLiveData()

    // user wait to count image
    val countAndState: MediatorLiveData<Pair<List<OnRouteUserImages>?, List<SheetItem.MarketNameAndState>?>>
        = MediatorLiveData()

    // user and friends progress
    val progressLiveData: MediatorLiveData<Pair<List<SheetItem.MarketNameAndState>?, List<FriendsProgress>?>>
            = MediatorLiveData()

    var userId = "yuyu11111"
    var friendId = "bb11111"


    fun getFriendsDetail(friendId: String) {
        repository.getUserDetail(object : FirebaseDataSource.UserCallBack {
            override fun onRoute(user: List<User>) {
                _usersDataList.value = user
            }
        }, friendId)
    }

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

    fun routePathsLatLngDrawPolyLine() {
        val pathList = mutableListOf<LatLng>()
        routeDataList.value?.paths?.forEach {
            val latLngToDouble = it.split(",")
            val lat = latLngToDouble[0].toDouble()
            val lng = latLngToDouble[1].toDouble()
            pathList.add(LatLng(lat, lng))
        }
        _routeDetailLatLngList.value = pathList
    }

    /**
     * snap route-images all that for check user finish this route or not
     */
    fun snapOnRouteUserImages(routeId: String) {
        repository.getUserRouteImages(object : FirebaseDataSource.UserRouteImagesCallBack {
            override fun onResult(imageList: List<OnRouteUserImages>) {
                if (_imagesLiveData.value == null || imageList.size > imagesLiveData.value!!.size) {
                    _imagesLiveData.value = imageList
                }
            }
        }, routeId)
    }

    fun snapOnRouteUserLocation(routeId: String) {
        repository.snapOnRoutePartner(object : FirebaseDataSource.UserRoutePartnerCallBack {
            override fun onResult(usersLocationList: List<OnRouteUserPartners>) {
                val locationList = mutableListOf<OnRouteUserPartners>()
                usersLocationList.forEach {
                    if(it.userId != userId) {
                        locationList.add(it)
                    }
                }
                _usersLocationLiveData.value = locationList
            }
        }, routeId)
    }

    /**
     * use mediatorLiveData put two LiveData together
     */
    fun addImageAndLatLngToPair() {
        imageImageAndLatLngLiveData.addSource(_marketDetailData) {
            imageImageAndLatLngLiveData.value = Pair(_imagesLiveData.value, it)
        }
        imageImageAndLatLngLiveData.addSource(_imagesLiveData) {
            imageImageAndLatLngLiveData.value = Pair(it, _marketDetailData.value)
        }
    }

    fun addCheckUserFinishedMediator () {
        checkUserFinishedLiveData.addSource(pointsIdList) {
            checkUserFinishedLiveData.value = Pair(_imagesLiveData.value, it)
        }
        checkUserFinishedLiveData.addSource(_imagesLiveData) {
            checkUserFinishedLiveData.value = Pair(it, pointsIdList.value)
        }
    }

    fun addCountAndStateMediatoe() {
        countAndState.addSource(_imagesLiveData) {
            countAndState.value = Pair(it, _marketNameAndStateFromDb.value)
        }
        countAndState.addSource(_marketNameAndStateFromDb) {
            countAndState.value = Pair(_imagesLiveData.value, it)
        }
    }

    fun waitProgressLiveData() {
        progressLiveData.addSource(_marketNameAndState) {
            progressLiveData.value = Pair(it, _friendsDataList.value)
        }
        progressLiveData.addSource(_friendsDataList) {
            progressLiveData.value = Pair(_marketNameAndState.value, it)
        }
    }

    fun getPointDetailList(pointIds: List<String>) {
        repository.getPointDetailList(object : FirebaseDataSource.PointCallBack {
            override fun onResult(list: List<PointData>) {
                _marketDetailData.value = list
            }
        }, pointIds)
    }

    fun countUserImage() {
        var count = 0
        imagesLiveData.value?.forEach {
            if (it.userId == userId) {
                count++
            }
        }

        _imageCount.value = count

        marketNameAndStateFromDb.value?.let {
            for(state in 0..count.minus(1)) {
                it[state].done = true
            }
            _marketNameAndState.value = it
        }
    }

    fun countFriendsImage(friendId: String) {
        var count = 0
        val list = mutableListOf<FriendsProgress>()
        imagesLiveData.value?.forEach {
            if(it.userId == friendId) {
                count++
            }
        }

        val friendsProgress = FriendsProgress(
            friendId,
            "https://p3-tt.byteimg.com/origin/pgc-image/46fcb4a9a4ad4bbba1719cc0ff40d074?from=pc",
            count
        )
        list.add(friendsProgress)
        _friendsDataList.value = list
    }

    //TODO: 確認user是否完成onRoute，true -> partner update 自己已完成
    fun checkUserFinished() {
        val count = imageCount.value
        _totalMarket.value = pointsIdList.value?.size
        pointsIdList.value?.let {
            if (count == it.size) {
                _finishedGame.value = true
                finishedGameToNull()
            }
        }
    }

    fun updatePartnerFinish() {

    }

    fun getNextPoint(): Pair<LatLng, String>? {
        val count = imageCount.value ?: 0

        var nextMarket: Pair<LatLng, String>? = null

        marketDetailData.value?.let {
            if (count < it.size ) {

                nextMarket = Pair(
                    LatLng(it[count].latitude, it[count].longitude),
                it[count].marketId)
                _nextName.value = it[count].name!!
            }
        }
        return nextMarket
    }

    fun nameAndStateInDataClass(nameList: List<String?>) {
        val list = mutableListOf<SheetItem.MarketNameAndState>()
        nameList?.forEach {
            val nameAndState = SheetItem.MarketNameAndState(
                it, false, null
            )
            list.add(nameAndState)
        }
        _marketNameAndStateFromDb.value = list
    }

    fun finishedGameToNull() {
        _finishedGame.value = null
    }

    companion object {
        val TAG = "BottomSheetViewModel"
    }
}