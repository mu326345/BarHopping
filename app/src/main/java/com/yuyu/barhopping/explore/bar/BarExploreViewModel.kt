package com.yuyu.barhopping.explore.bar

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.Bar
import com.yuyu.barhopping.data.RouteCommend
import kotlin.math.absoluteValue

class BarExploreViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _barItem = MutableLiveData<List<Bar>>()
    val barItem: LiveData<List<Bar>>
        get() = _barItem

    private val list = mutableListOf<Bar>()

    init {
        snapBarData()
    }


    private fun snapBarData() {
        db.collection("Bar").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (x in snapshot.documents) {
                    val barList = Bar(
                        id = x["id"] as String,
                        senderId = x["senderId"] as String,
//                        double doubleValue = document.getDouble("myDoubleProperty")
//                        locationX = x["locationX"] as Double,
//                        locationY = x["locationY"] as Double,
                        locationX = x.getDouble("locationX") as Double,
                        locationY = x.getDouble("locationY") as Double,
                        name = x["name"] as String,
                        address = x["address"] as String,
                        image = x["image"] as String,
                        commend = x["commend"] as String,
                        phone = x["phone"] as String
                    )
                    list.add(barList)
                }
                _barItem.value = list
            }
        }
    }

    private fun toDouble(long: Long): Double {
        return long.toDouble()
    }
}