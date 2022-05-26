package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BarPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImg: String = "",
    val uri: String = "",
    val commend: String = "",
    val barName: String ?= null,
    val barId: String = "",
    val barAddress: String ?= null,
    val barLat: String = "",
    val barLng: String = "",
    val barPhone: String ?= null,
    val time: String = ""
) : Parcelable
