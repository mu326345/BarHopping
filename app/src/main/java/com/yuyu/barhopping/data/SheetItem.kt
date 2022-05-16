package com.yuyu.barhopping.data

data class SheetItem(
    val name: String?,
    var done: Boolean,
    var count: Int,
    var partners: MutableList<Partner> = mutableListOf()
)
