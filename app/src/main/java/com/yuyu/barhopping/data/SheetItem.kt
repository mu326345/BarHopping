package com.yuyu.barhopping.data

sealed class SheetItem {
    data class MarketNameAndState(
        val name: String?,
        var done: Boolean,
        var friends: List<FriendsProgress>?
    ): SheetItem()
}
