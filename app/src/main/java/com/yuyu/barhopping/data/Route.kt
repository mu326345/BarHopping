package com.yuyu.barhopping.data


data class Route(
    val id: String,
    val startPoint: String,
    val endPoint: String,
    val points: String,
    val comments: String,
    val hardDegree: Long,
    val length: Long
)