package com.yuyu.barhopping.data

data class Bar(
    val id: String,
    val senderId: String,
    val locationX: Double,
    val locationY: Double,
    val name: String,
    val address: String,
    val image: String,
    val commend: String,
    val phone: String,
    val like: Long
)