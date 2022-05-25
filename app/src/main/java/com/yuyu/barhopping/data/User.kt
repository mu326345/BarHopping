package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String = "", //google id
    val name: String = "",
    val title: String = "",
    val icon: String = "", //URL img
    val routeCollection: String = "", // List<Route id>
    val userCollection: String = "", // List<User id>
    var onRoute: String? = null,
    var barPost: List<String>? = emptyList(),
    var marketCount: List<Int>? = listOf(0)
) : Parcelable