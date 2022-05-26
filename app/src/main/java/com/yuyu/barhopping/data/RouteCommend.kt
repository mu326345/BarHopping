package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RouteCommend(
    val id: String = "",
    val senderId: String = "",
    val userHardDegree: Long = -1,
    val commend: String = "",
    val image: String = "",
    val commendTime: String = "",
    val like: Long = -1
) : Parcelable
