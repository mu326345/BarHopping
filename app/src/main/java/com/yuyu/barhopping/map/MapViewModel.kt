package com.yuyu.barhopping.map

import android.graphics.Color
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.SphericalUtil
import com.yuyu.barhopping.network.DirectionApi
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import kotlin.math.max

class MapViewModel : ViewModel() {

    private val _moveCameraLiveData = MutableLiveData<LatLng>()
    val moveCameraLiveData: LiveData<LatLng>
        get() = _moveCameraLiveData

    private val _lastLocationLiveData = MutableLiveData<Location>()
    val lastLocationLiveData: LiveData<Location>
        get() = _lastLocationLiveData

    private val _desMarkerLiveData = MutableLiveData<LatLng>()
    val desMarkerLiveData: LiveData<LatLng>
        get() = _desMarkerLiveData

    private val _paths = MutableLiveData<List<LatLng>>()
    val paths: LiveData<List<LatLng>>
        get() = _paths

    private val _marketResult = MutableLiveData<List<LatLng>>()
    val marketResult: LiveData<List<LatLng>>
        get() = _marketResult

    private val _selectLocationName = MutableLiveData<String>("")
    val selectLocationName: LiveData<String>
        get() = _selectLocationName

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
    }

    fun onPoiClick(poi: PointOfInterest) {
        _desMarkerLiveData.value = poi.latLng
        _moveCameraLiveData.value = poi.latLng
        _selectLocationName.value = poi.name
    }

    fun onPlaceSelect(place: Place) {
        _desMarkerLiveData.value = place.latLng
        _moveCameraLiveData.value = place.latLng
        _selectLocationName.value = place.name
    }

    fun showDirection() {
        viewModelScope.launch {
            try {
                val directionResult = DirectionApi.retrofitService.getDirectionResult(
                    "${lastLocationLiveData.value!!.latitude},${lastLocationLiveData.value!!.longitude}",
                    "${desMarkerLiveData.value!!.latitude},${desMarkerLiveData.value!!.longitude}"
                )

                val path = mutableListOf<LatLng>()
                for (i in 0 until (directionResult.routes[0].legs[0].steps.size - 1)) {
                    val startLatLng = LatLng(
                        directionResult.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
                        directionResult.routes[0].legs[0].steps[i].start_location.lng.toDouble()
                    )
                    path.add(startLatLng)
                    val endLatLng = LatLng(
                        directionResult.routes[0].legs[0].steps[i].end_location.lat.toDouble(),
                        directionResult.routes[0].legs[0].steps[i].end_location.lng.toDouble()
                    )
                    path.add(endLatLng)
                }
                _paths.value = path

            } catch (e: NullPointerException) {
                Log.e("MapViewModel", "NullPointerException")
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onLocationUpdate(location: Location) {
        _lastLocationLiveData.value = location
    }

    // nearby api find 7-11
    fun showMarketItems() {
        viewModelScope.launch {
            paths.value?.let {
                try {
                    val latlng = it[it.size / 2]
                    val radiusA = SphericalUtil.computeDistanceBetween(it[0], latlng)
                    val radiusB = SphericalUtil.computeDistanceBetween(it[it.size-1], latlng)
                    val radius = max(radiusA, radiusB).toInt()
                    val marketResult = DirectionApi.retrofitService.getNearbyMarket(
                        "${latlng.latitude},${latlng.longitude}",
                        radius,
                        "7-11"
                        )
                    Log.v("QAQ", "${marketResult.results.map { it.name }}")
                    _marketResult.value = marketResult.results.map { LatLng(it.geometry.location.lat, it.geometry.location.lng)}

                } catch (e: NullPointerException) {
                    Log.e("MapViewModel", "NullPointerException")
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}