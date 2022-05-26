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
    val barPost: List<String>? = emptyList(),
    val marketCount: List<Int>? = emptyList()
) : Parcelable