package com.yuyu.barhopping.data

import com.google.android.gms.maps.model.MarkerOptions

data class MarketMarkerDataItem(
    val markerName: MarketName,
    val placeId: String,
    val marketOptions: MarkerOptions
)
