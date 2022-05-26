package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String = "", //google id
    val name: String = "",
    val title: String = "",
    val icon: String = "", //URL img
    var onRoute: String? = null,
    var barPost: List<String>? = emptyList(),
    var marketCount: List<Int>? = listOf(0)
) : Parcelable