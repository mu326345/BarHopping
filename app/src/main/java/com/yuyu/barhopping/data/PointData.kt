package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PointData(
    val latitude: Double,
    val longitude: Double,
    val marketId: String,
    val name: String?,
    val type: Int?
) : Parcelable
