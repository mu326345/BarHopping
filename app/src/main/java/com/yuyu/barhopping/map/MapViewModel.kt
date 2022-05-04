package com.yuyu.barhopping.map

import android.content.ContentValues
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import com.yuyu.barhopping.data.*
import com.yuyu.barhopping.network.DirectionApi
import com.yuyu.barhopping.util.getMarketType
import kotlinx.coroutines.launch
import kotlin.math.max

class MapViewModel : ViewModel() {

    private val db = Firebase.firestore

    private var userDocId: String? = null

    private var storeRouteDocId: String? = null

    private var userOnRouteLocationId: String? = null

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
    private val _onRoute = MutableLiveData<Boolean>()
    val onRoute: LiveData<Boolean>
        get() = _onRoute

    private val _userOnRouteId = MutableLiveData<String>()
    val userOnRouteId: LiveData<String>
        get() = _userOnRouteId


    var userId = "yuyu11111"

    init {
        userId = "yuyu11111"
        checkUserOnRoute()
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
    fun showDirection() { //(list: List<LatLng>)
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

                    val orderList = directionResult.routes[0].waypoint_order

                    if (orderList.size > 0) {
                        val nonOrderlist = directionResult.geocoded_waypoints.map {
                            it.place_id.toString()
                        }
                        val orderPlaceIdList = mutableListOf<String>()

                        for (x in 0..orderList.size - 1) {
                            val pos = orderList.indexOf(x)
                            orderPlaceIdList.add(nonOrderlist[pos])
                        }

                        // point place Id list
                        routePlaceIdList.clear()
                        routePlaceIdList.addAll(orderPlaceIdList)
                        routePlaceIdList.removeAt(0)
                        routePlaceIdList.removeAt(routePlaceIdList.size - 1)
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
            marker.tag.toString(),
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
        _onRoute.value = true
    }

    fun notOnRouting() {
        _onRoute.value = false

        userDocId?.let {
            db.collection("User")
                .document(userDocId!!)
                .update("onRoute", null)
        }
    }

    private fun checkUserOnRoute() {
        db.collection("User")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (x in documents) {
                    Log.d(TAG, "${x.data["name"]} => ${x.data}")
                    userDocId = x.id
                    _userOnRouteId.value = x.data["onRoute"] as String?
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    fun storeOnRoute() {
        storeRouteDocId?.let {
            var newDocRef = db.collection("Routes").document()

            routePlaceIdList
            val pathsSize = paths.value!!.size -1
            val routePlaceIdListSize = routePlaceIdList.size.toLong()
            val onRoute = RouteStore(
                id = newDocRef.id,
                startPoint = "起點",
                startLat = "${paths.value?.get(0)?.latitude}",
                startLon = "${paths.value?.get(0)?.longitude}",
                endPoint = desMarkerNameLiveData.value!!,
                endLat = "${paths.value?.get(pathsSize)?.latitude}",
                endLon = "${paths.value?.get(pathsSize)?.longitude}",
                marketCount = routePlaceIdListSize,
                length = distance,
                hardDegree = null,
                comments = null,
                points = routePlaceIdList,
                paths = pathsLatLngListStr
            )
            storeRouteDocId = newDocRef.id
            newDocRef.set(onRoute)
            uploadOuRouteIdToUser(storeRouteDocId!!)

            storePointDetail()
            updateSheetData()
        }
    }

    private fun storePointDetail() {
        val selectPointIds = selectMarketData.map {
            it.marketId
        }

        var existPointIdList = mutableListOf<String>()
        db.collection("Points")
            .whereIn(FieldPath.documentId(), selectPointIds)
            .get()
            .addOnSuccessListener { documents ->
                documents.map { it.id }.forEach {
                    existPointIdList.add(it)
                }

                val diffrentIdDatas = selectMarketData.filterNot { select ->
                    existPointIdList.contains(select.marketId)
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
     * add user location first time
     */
    fun addUserLocationToOnRoute() {
        storeRouteDocId?.let {
            val locationRef = db.collection("Routes")
                .document(storeRouteDocId!!)
                .collection("locations")
                .document()

            val locationData = OnRouteUserLocation(
                id = locationRef.id,
                userId = userId,
                lat = lastLocationLiveData.value?.latitude.toString(),
                lng = lastLocationLiveData.value?.longitude.toString()
            )
            userOnRouteLocationId = locationRef.id
            locationRef.set(locationData)
        }
    }

    /**
     * update user location
     */
    fun updateUserLocationToOnRoute() {
        if(storeRouteDocId != null && userOnRouteLocationId != null) {
            val locationData = OnRouteUserLocation(
                id = userOnRouteLocationId!!,
                userId = userId,
                lat = lastLocationLiveData.value?.latitude.toString(),
                lng = lastLocationLiveData.value?.longitude.toString()
            )

            db.collection("Routes")
                .document(storeRouteDocId!!)
                .collection("locations")
                .document(userOnRouteLocationId!!)
                .set(locationData)
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

    companion object {
        val TAG = "MapViewModel"
    }
}