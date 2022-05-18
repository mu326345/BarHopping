package com.yuyu.barhopping.util

import android.util.Log
import com.yuyu.barhopping.data.MarketName
import java.text.SimpleDateFormat
import java.util.*


fun String.getMarketType(): Int {
    val name = this

    if (name.contains("7-Eleven", true)
        || name.contains("小7", true)
        || name.contains("711", true))  {

        return 1
    } else if (name.contains("全家", true)
        || name.contains("FamilyMart", true))  {

        return 2
    } else if (name.contains("萊爾富", true)
        || name.contains("Hilife", true))  {

        return 3
    } else if (name.contains("OK", true))  {

        return 4
    } else {
        return 0
    }
}

fun timeNow(): String {
    val now = Calendar.getInstance(Locale.TAIWAN).timeInMillis
    val format = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = format.format(now)

    return currentDate.toString()
}