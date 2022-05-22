package com.yuyu.barhopping.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RouteStore(
    val id: String?,
    val startPoint: String,
    val startLat: String,
    val startLon: String,
    val endPoint: String,
    val endLat: String,
    val endLon: String,
    val marketCount: Long,
    val length: Long?,
    val hardDegree: Int? = 0,
    val comments: String?,
    val points: List<String>? = null,
    val paths: List<String>? = null,
    val time: String?,
    val userName: String?,
    val userIcon: String?,
    val userId: String?
) : Parcelable

@Parcelize
data class NewRouteStore(
    val id: String?,
    val startPoint: String,
    val startLat: String,
    val startLon: String,
    val endPoint: String,
    val endLat: String,
    val endLon: String,
    val marketCount: Long,
    val length: Long?,
    val hardDegree: Int? = 0,
    val comments: String?,
    val points: List<String>? = null,
    val paths: List<String>? = null,
    val time: String?,
    val userName: String?,
    val userIcon: String?,
    val userId: String?,
    var userLike:Boolean
) : Parcelable