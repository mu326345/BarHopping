package com.yuyu.barhopping.data


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
    val points: List<String>?,
    val paths: List<String>?
)