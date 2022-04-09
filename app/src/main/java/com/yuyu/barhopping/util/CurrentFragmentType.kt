package com.yuyu.barhopping.util

import com.yuyu.barhopping.R

enum class CurrentFragmentType(val value: String, val index: Int) {
    MAP("${R.string.map}", 1),
    CHAMP("${R.string.champ}", 2),
    POST("${R.string.post}", 3),
    EXPLORE("${R.string.explore}", 4),
    PROFILE("${R.string.profile}", 5)
}