package com.yuyu.barhopping.data

import com.google.android.gms.maps.model.MarkerOptions

data class AddMarkerData(
    val markerName: MarketName,
    val placeIds: List<String>,
    val marketOptions: List<MarkerOptions>
)
