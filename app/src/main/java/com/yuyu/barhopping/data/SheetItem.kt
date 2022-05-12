package com.yuyu.barhopping.data

data class SheetItem(
    val name: String?,
    var done: Boolean,
    var users: MutableList<User> = mutableListOf()
)
