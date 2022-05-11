package com.yuyu.barhopping.map

import android.content.ContentValues
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.SphericalUtil
import com.yuyu.barhopping.data.*
import com.yuyu.barhopping.network.DirectionApi
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource
import com.yuyu.barhopping.util.getMarketType
import kotlinx.coroutines.launch
import kotlin.math.max

class MapViewModel(val repository: FirebaseRepository) : ViewModel() {

    private val db = Firebase.firestore

    private val storage = Firebase.storage

    private var userDocId: String? = null

    private var storeRouteDocId: String? = null

    private var userOnRoutePartnerId: String? = null

    private val _moveCameraLiveData = MutableLiveData<LatLng>()
    val moveCameraLiveData: LiveData<LatLng>
        get() = _moveCameraLiveData

    var originToDesList = mutableListOf<LatLng>()

    val selectMarketData = mutableListOf<PointData>()

    private val _lastLocationLiveData = MutableLiveData<Location>()
    val lastLocationLiveData: LiveData<Location>
        get() = _lastLocationLiveData

    private val _desMarkerLiveData = MutableLiveData<LatLng>()
    val desMarkerLiveData: LiveData<LatLng>
        get() = _desMarkerLiveData

    private val _desMarkerNameLiveData = MutableLiveData<String>()
    val desMarkerNameLiveData: LiveData<String>
        get() = _desMarkerNameLiveData

    val selectLocationName = MutableLiveData<String>()

    private val _paths = MutableLiveData<List<LatLng>>()
    val paths: LiveData<List<LatLng>>
        get() = _paths

    var pathsLatLngListStr = mutableListOf<String>()

    var distance: Long? = null

    private val _addMarkerLiveData = MutableLiveData<AddMarkerData>()
    val addMarkerLiveData: LiveData<AddMarkerData>
        get() = _addMarkerLiveData

    private val _userSelectMarketName = MutableLiveData<List<String?>>()
    val userSelectMarketName: LiveData<List<String?>>
        get() = _userSelectMarketName

    var routePlaceIdList = mutableListOf<String>()

    val sevenChecked = MutableLiveData<Boolean>()
    val familyChecked = MutableLiveData<Boolean>()
    val hiLifeChecked = MutableLiveData<Boolean>()
    val okMartChecked = MutableLiveData<Boolean>()

    private val _visibleMarker = MutableLiveData<Pair<MarketName, Boolean>>()
    val visibleMarker: LiveData<Pair<MarketName, Boolean>>
        get() = _visibleMarker

    private val _stepPage = MutableLiveData<Int>(StepTypeFilter.STEP1.index)
    val stepPage: LiveData<Int>
        get() = _stepPage

    // handle user on game or not
    private val _onRoute = MutableLiveData<Boolean>(false)
    val onRoute: LiveData<Boolean>
        get() = _onRoute

    private val _userOnRouteId = MutableLiveData<String>()
    val userOnRouteId: LiveData<String>
        get() = _userOnRouteId

    private val _userDetailLiveData = MutableLiveData<User>()
    val userDetailLiveData: LiveData<User>
        get() = _userDetailLiveData


    var userId = "yuyu11111"

    init {
        userId = "yuyu11111"
        getUserData()
    }

    fun onLocationBtnClick() {
        if (lastLocationLiveData.value != null) {
            _moveCameraLiveData.value = LatLng(
                lastLocationLiveData.value!!.latitude,
                lastLocationLiveData.value!!.longitude
            )
        }
    }

    fun onLastLocationUpdated(location: Location) {
        _lastLocationLiveData.value = location
        _moveCameraLiveData.value = LatLng(location.latitude, location.longitude)
        originToDesList.add(0, LatLng(location.latitude, location.longitude))
    }

    fun onPoiClick(poi: PointOfInterest) {
        when (stepPage.value) {
            StepTypeFilter.STEP1.index -> {
                _desMarkerLiveData.value = poi.latLng
                _moveCameraLiveData.value = poi.latLng
                selectLocationName.value = poi.name
                _desMarkerNameLiveData.value = poi.name
                originToDesList.add(1, poi.latLng)
            }
            StepTypeFilter.STEP2.index -> {
                _stepPage.value = StepTypeFilter.STEP1.index
                _desMarkerLiveData.value = poi.latLng
                _moveCameraLiveData.value = poi.latLng
                selectLocationName.value = poi.name
                _desMarkerNameLiveData.value = poi.name
                originToDesList.add(1, poi.latLng)
            }
            StepTypeFilter.STEP3.index -> {

            }
        }
    }

    fun onPlaceSelect(place: Place) {
        _desMarkerLiveData.value = place.latLng
        _desMarkerNameLiveData.value = place.name
        _moveCameraLiveData.value = place.latLng
        selectLocationName.value = place.name
        place.latLng?.let {
            originToDesList.add(1, it)
        }
    }

    // show direction and draw route
    fun showDirection() {
        viewModelScope.launch {
            val ori =
                "${lastLocationLiveData.value!!.latitude},${lastLocationLiveData.value!!.longitude}"
            val des = "${desMarkerLiveData.value!!.latitude},${desMarkerLiveData.value!!.longitude}"

            var waypoints: String = ""
            val selectMarketId = selectMarketData.map {
                it.marketId
            }
            val tempList = selectMarketId.map { "place_id:$it" }

            waypoints = "optimize:true|"+tempList.joinToString(separator = "|")

            try {
                val directionResult = DirectionApi.retrofitService.getDirectionResult(
                    ori,
                    des,
                    waypoints
                )

                distance = countDistance(directionResult).toLong()

                if (directionResult.routes.size <= 0) {
                    Log.w("MapViewModel", "No routes")
                } else {
                    val path = mutableListOf<LatLng>()
                    var alreadyAddStart = false
                    for (i in 0 until (directionResult.routes[0].legs.size)) {
                        for (j in 0 until directionResult.routes[0].legs[i].steps.size) {
                            if (!alreadyAddStart) {
                                val startLatLng = LatLng(
                                    directionResult.routes[0].legs[i].steps[j].start_location.lat.toDouble(),
                                    directionResult.routes[0].legs[i].steps[j].start_location.lng.toDouble()
                                )
                                path.add(startLatLng)
                                alreadyAddStart = true
                            }
                            val endLatLng = LatLng(
                                directionResult.routes[0].legs[i].steps[j].end_location.lat.toDouble(),
                                directionResult.routes[0].legs[i].steps[j].end_location.lng.toDouble()
                            )
                            path.add(endLatLng)
                        }
                    }

                    // api 自動排序 value = Int
                    val apiOrderList = directionResult.routes[0].waypoint_order
                    // 還沒排序marketId
                    if (apiOrderList.size > 0) {
                        val nonOrderlist = directionResult.geocoded_waypoints.filterIndexed { index ,it ->
                            index >= 1 && index < directionResult.geocoded_waypoints.size-1
                        }.map {
                            it.place_id.toString()
                        }

                        // 排序後的list
                        val orderPlaceIdList = mutableListOf<String>()
                        for(x in 0 until apiOrderList.size) {
                            apiOrderList.forEach {
                                if(x == it) {
                                    orderPlaceIdList.add(nonOrderlist[x])
                                }
                            }
                        }

                        // point place Id list
                        routePlaceIdList.clear()
                        routePlaceIdList.addAll(orderPlaceIdList)
                    }

                    _paths.value = path

                    // poly line list
                    val pathsLatLngList = mutableListOf<String>()
                    pathsLatLngListStr.clear()
                    paths.value?.forEach {
                        pathsLatLngList.add("${it.latitude},${it.longitude}")
                    }
                    pathsLatLngListStr.addAll(pathsLatLngList)
                }

            } catch (e: NullPointerException) {
                Log.e("MapViewModel", "NullPointerException")
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun countDistance(directionResult: GoogleMapDTO): Int {
        var distance = 0
        for (x in 0 until (directionResult.routes[0].legs.size)) {
            distance += directionResult.routes[0].legs[x].distance.value
        }
        return distance
    }

    fun onLocationUpdate(location: Location) {
        _lastLocationLiveData.value = location
    }

    fun setMarketCheck(market: MarketName, checked: Boolean, hasData: Boolean, bitmap: Bitmap) {
        if (checked) {
            if (hasData) {
                _visibleMarker.value = Pair(market, true)
            } else {
                showMarketItems(market, bitmap)
            }
        } else {
            if (hasData) {
                _visibleMarker.value = Pair(market, false)
            }
        }
    }

    // nearby api find market
    fun showMarketItems(marketName: MarketName, bitmap: Bitmap) {
        viewModelScope.launch {
            paths.value?.let {
                try {
                    val latlng = it[it.size / 2]
                    val radiusA = SphericalUtil.computeDistanceBetween(it[0], latlng)
                    val radiusB = SphericalUtil.computeDistanceBetween(it[it.size - 1], latlng)
                    val radius = max(radiusA, radiusB).toInt()
                    val marketResult = DirectionApi.retrofitService.getNearbyMarket(
                        "${latlng.latitude},${latlng.longitude}",
                        radius,
                        marketName.value //"7-11, 全家" keyword
                    )

                    _addMarkerLiveData.value = AddMarkerData(
                        marketName,
                        marketResult.results.map {
                            it.place_id
                        },
                        marketResult.results.map {
                            MarkerOptions()
                            .position(LatLng(it.geometry.location.lat, it.geometry.location.lng))
                            .title(it.name)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    })
                } catch (e: NullPointerException) {
                    Log.e("MapViewModel", "NullPointerException")
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setStepPage(step: StepTypeFilter) {
        _stepPage.value = step.index
        when (step) {
            StepTypeFilter.STEP1 -> null
            StepTypeFilter.STEP2 -> {
                showDirection()
            }
            StepTypeFilter.STEP3 -> {
                showDirection()
            }
        }
    }

    fun onMarkerClick(marker: Marker) {
        var exist = false
        val marketData = PointData(
            marker.position.latitude,
            marker.position.longitude,
            marker.tag.toString(),// MarketId
            marker.title,
            marker.title?.getMarketType()
        )

        for (x in selectMarketData) {
            if (LatLng(marker.position.latitude, marker.position.longitude).equals(x)) {
                exist = true
            }
        }

        if (exist) {
            selectMarketData.remove(marketData)
            marker.alpha = 1f
        } else {
            selectMarketData.add(marketData)
            marker.alpha = 0.3f
        }
    }

    fun onRouting() {
        if (_onRoute.value == false) {
            _onRoute.value = true
        }
    }

    /**
     * 清空user onRoute的紀錄
     */
    fun notOnRouting() {
        _onRoute.value = false

        // 清空user onRoute Id
        userDocId?.let {
            db.collection("User")
                .document(userDocId!!)
                .update("onRoute", null)
        }
    }

    /**
     *  init this fun
     *  存取user 資料，確認是否有onRoute路線
     *  如果開始遊戲時，會再call this fun again then
     */
    fun getUserData() {
        repository.getUserDetail(object : FirebaseDataSource.UserCallBack {
            override fun onRoute(user: List<User>) {
                user.forEach {
                    _userDetailLiveData.value = it
                    userDocId = it.id //userDocId == userGoogleId
                    _userOnRouteId.value = it.onRoute?: null
                }
            }
        }, userId)
    }

    fun storeOnRoute() {
//        storeRouteDocId?.let {
        var newDocRef = db.collection("Routes").document()

        routePlaceIdList?.let {
            val pathsSize = paths.value!!.size - 1
//            val routePlaceIdListSize = it.size
            val onRoute = RouteStore(
                id = newDocRef.id,
                startPoint = "起點",
                startLat = "${paths.value?.get(0)?.latitude}",
                startLon = "${paths.value?.get(0)?.longitude}",
                endPoint = desMarkerNameLiveData.value!!,
                endLat = "${paths.value?.get(pathsSize)?.latitude}",
                endLon = "${paths.value?.get(pathsSize)?.longitude}",
                marketCount = it.size.toLong(),
                length = distance,
                hardDegree = null,
                comments = null,
                points = it,
                paths = pathsLatLngListStr
            )
            newDocRef.set(onRoute)
            storeRouteDocId = newDocRef.id
            uploadOuRouteIdToUser(newDocRef.id)
        }

        storePointDetail()
        updateSheetData()
//        }
    }

    private fun storePointDetail() {
        val selectPointIds = selectMarketData.map {
            it.marketId
        }

        db.collection("Points")
            .whereIn(FieldPath.documentId(), selectPointIds) // 1.找出相同pointDocId list
            .get()
            .addOnSuccessListener { documents ->
                var existPointIdList = mutableListOf<String>()

                documents.map { it.id }.forEach {
                    existPointIdList.add(it) // 2.儲存已經存在的DocId list
                }

                val diffrentIdDatas = selectMarketData.filterNot { select ->
                    existPointIdList.contains(select.marketId) //3. 找出不一樣的DocId
                }

                diffrentIdDatas.forEach {
                    db.collection("Points").document(it.marketId)
                        .set(it)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    /**
     * 第一次，在onRoute 新增partner 文件
     */
    fun addPartnerToOnRoute() {
        storeRouteDocId?.let {
            val partnerRef = db.collection("Routes")
                .document(it)
                .collection("Partners")
                .document()

            userDetailLiveData.value?.let {
                val partnerData = OnRouteUserPartners(
                    id = partnerRef.id,
                    userId = userId,
                    lat = lastLocationLiveData.value?.latitude.toString(),
                    lng = lastLocationLiveData.value?.longitude.toString(),
                    name = it.name,
                    imageUrl = it.icon,
                    finished = false
                )
                userOnRoutePartnerId = partnerRef.id
                partnerRef.set(partnerData)
            }

        }
    }

    /**
     * update user location
     */
    fun updatePartnerLocation() {
        storeRouteDocId?.let { routeDocId ->
            userOnRoutePartnerId?.let { partnerDocId ->
                db.collection("Routes")
                    .document(routeDocId)
                    .collection("Partners")
                    .document(partnerDocId)
                    .update(
                        "lat", lastLocationLiveData.value?.latitude.toString(),
                        "lng", lastLocationLiveData.value?.longitude.toString()
                    )
            }
        }
    }

    private fun updateSheetData() {
        val nameList = mutableListOf<String?>()
        routePlaceIdList.forEach {
            selectMarketData.forEach { p ->
                if (it == p.marketId) {
                    nameList.add(p.name)
                }
            }
        }
        _userSelectMarketName.value = nameList
    }

    private fun uploadOuRouteIdToUser(onRoute: String) {
        userDocId?.let {
            db.collection("User")
                .document(userDocId!!)
                .update("onRoute", onRoute)
                .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error updating document", e) }
        }
    }

    fun uploadImageUriToStorage(onRouteId: String, uri: Uri, pointId: String) {
        var storageRef = storage.reference
        val imagesRef = storageRef.child("images/${uri.lastPathSegment}")
        val uploadTask = imagesRef.putFile(uri)

        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imagesRef.downloadUrl

            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    uploadImageUriToDB(onRouteId, downloadUri, pointId)
                } else {
                    // Handle failures
                }
            }
        }
    }

    fun uploadImageUriToDB(onRouteId: String, uri: Uri, pointId: String) {
        val imageRef = db.collection("Routes")
            .document(onRouteId)
            .collection("images")
            .document()

        val image = OnRouteUserImages(
            id = imageRef.id,
            pointId = pointId,
            url = uri.toString(),
            userId = userId
        )

        imageRef.set(image)
    }

    companion object {
        val TAG = "MapViewModel"
    }


    fun canTakePhoto(currentLatLng: LatLng, nextLatLng: LatLng
    ): Boolean {
        val distance = SphericalUtil.computeDistanceBetween(
            currentLatLng, nextLatLng
        ).toInt()

        return distance <= 20
//        return true
    }
}