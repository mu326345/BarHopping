package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Partner(
    val id: String = "",
    val userId: String = "",
    val lat: String = "",
    val lng: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val finished: Boolean = false
) : Parcelable