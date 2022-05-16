package com.yuyu.barhopping.data

import com.google.android.gms.maps.model.LatLng

data class ReadyToRoute(
    var startPoint: LatLng? = null,
    var destinationPoint: LatLng? = null,
    var startName: String? = null,
    var destinationName: String? = null,
    val points: MutableList<PointData> = mutableListOf(),
    var paths: MutableList<String> = mutableListOf(),
    var sortedPlaceIds: MutableList<String> = mutableListOf(),
    var distance: Long? = null
)
