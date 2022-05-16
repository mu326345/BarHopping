package com.yuyu.barhopping.data

data class Partner(
    val id: String,
    val userId: String,
    val lat: String,
    val lng: String,
    val name: String,
    val imageUrl: String,
    val finished: Boolean
)