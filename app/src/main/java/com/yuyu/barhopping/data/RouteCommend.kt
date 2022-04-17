package com.yuyu.barhopping.data

data class RouteCommend(
    val id: String,
    val senderId: String,
    val userHardDegree: Long,
    val commend: String,
    val image: String,
    val commendTime: String,
    val like: Long
)
