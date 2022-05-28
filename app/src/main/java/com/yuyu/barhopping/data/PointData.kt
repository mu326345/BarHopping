package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PointData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val marketId: String = "",
    val name: String = "",
    val type: Int? = null
) : Parcelable
