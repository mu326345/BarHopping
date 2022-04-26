package com.yuyu.barhopping.data

data class User(
    val id: String, //google id
    val name: String,
    val title: String,
    val icon: String, //URL img
    val routeCollection: String, // List<Route id>
    val userCollection: String, // List<User id>
    val onRoute: String
)