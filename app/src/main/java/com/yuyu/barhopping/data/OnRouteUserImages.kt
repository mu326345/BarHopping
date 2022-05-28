package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OnRouteUserImages(
    val id: String = "",
    val pointId: String = "",
    val url: String = "",
    val userId: String = ""
) : Parcelable
