package com.yuyu.barhopping.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.SphericalUtil
import com.yuyu.barhopping.data.GoogleMapDTO
import com.yuyu.barhopping.data.MarketName
import com.yuyu.barhopping.network.DirectionApi
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import kotlin.math.max

class MapViewModel : ViewModel() {

    private val _moveCameraLiveData = MutableLiveData<LatLng>()
    val moveCameraLiveData: LiveData<LatLng>
        get() = _moveCameraLiveData

    var originToDesList = mutableListOf<LatLng>()
    val markerList = mutableListOf<LatLng>()

    private val _lastLocationLiveData = MutableLiveData<Location>()
    val lastLocationLiveData: LiveData<Location>
        get() = _lastLocationLiveData

    private val _desMarkerLiveData = MutableLiveData<LatLng>()
    val desMarkerLiveData: LiveData<LatLng>
        get() = _desMarkerLiveData

    private val _desMarkerNameLiveData = MutableLiveData<String>()
    val desMarkerNameLiveData: LiveData<String>
        get() = _desMarkerNameLiveData

    private val _paths = MutableLiveData<List<LatLng>>()
    val paths: LiveData<List<LatLng>>
        get() = _paths

    private val _addMarkerLiveData = MutableLiveData<Pair<MarketName, List<MarkerOptions>>>()
    val addMarkerLiveData: LiveData<Pair<MarketName, List<MarkerOptions>>>
        get() = _addMarkerLiveData

    val selectLocationName = MutableLiveData<String>()

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
                originToDesList.add(1, poi.latLng)
            }
            StepTypeFilter.STEP2.index -> {
                _stepPage.value = StepTypeFilter.STEP1.index
                _desMarkerLiveData.value = poi.latLng
                _moveCameraLiveData.value = poi.latLng
                selectLocationName.value = poi.name
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
        originToDesList.add(1, place.latLng)
    }

    // show direction and draw route
    fun showDirection(list: List<LatLng>) {
        viewModelScope.launch {
            val ori = "${lastLocationLiveData.value!!.latitude},${lastLocationLiveData.value!!.longitude}"
            val des = "${desMarkerLiveData.value!!.latitude},${desMarkerLiveData.value!!.longitude}"

            var waypoints:String = ""
            val tempList = mutableListOf<String>()
            for (x in list) {
                tempList.add("${x.latitude},${x.longitude}")
            }
            waypoints = tempList.joinToString(separator = "|")

            try {
                val directionResult = DirectionApi.retrofitService.getDirectionResult(
                    ori,
                    des,
                    waypoints
                )

                countDistance(directionResult)

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
                    _paths.value = path
                }

            } catch (e: NullPointerException) {
                Log.e("MapViewModel", "NullPointerException")
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun countDistance(directionResult: GoogleMapDTO) {
        var distance = 0
        for(x in 0 until (directionResult.routes[0].legs.size)) {
            distance += directionResult.routes[0].legs[x].distance.value
        }

    }

    fun onLocationUpdate(location: Location) {
        _lastLocationLiveData.value = location
    }

    fun setMarketCheck(market: MarketName, checked: Boolean, hasData: Boolean) {
        if (checked) {
            if (hasData) {
                _visibleMarker.value = Pair(market, true)
            } else {
                showMarketItems(market)
            }
        } else {
            if (hasData) {
                _visibleMarker.value = Pair(market, false)
            }
        }
    }

    // nearby api find market
    fun showMarketItems(market: MarketName) {
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
                        market.value //"7-11, 全家"
                    )

                    _addMarkerLiveData.value = Pair(market, marketResult.results.map {
                        MarkerOptions()
                            .position(LatLng(it.geometry.location.lat, it.geometry.location.lng))
                            .title(it.name)
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
                showDirection(listOf())
            }
            StepTypeFilter.STEP3 -> {
                showDirection(markerList)
            }
        }
    }

    fun onMarkerClick(marker: Marker) {
        var exist = false
        for (x in markerList) {
            if (LatLng(marker.position.latitude, marker.position.longitude).equals(x)) {
                exist = true
            }
        }
        if (exist) {
            markerList.remove(LatLng(marker.position.latitude, marker.position.longitude))
            marker.alpha = 1f
        } else {
            markerList.add(LatLng(marker.position.latitude, marker.position.longitude))
            marker.alpha = 0.3f
        }
    }
}