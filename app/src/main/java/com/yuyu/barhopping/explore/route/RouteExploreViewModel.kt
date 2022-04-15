package com.yuyu.barhopping.explore.route

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.RouteCommend

class RouteExploreViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _routeCommendItem = MutableLiveData<List<RouteCommend>>()
    val routeCommendItem: LiveData<List<RouteCommend>>
        get() = _routeCommendItem

    private val list = mutableListOf<RouteCommend>()

    init {
        snapRouteCommendData()
    }


    private fun snapRouteCommendData() {
        db.collection("RouteCommend").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (x in snapshot.documents) {
                    val routeCommendList = RouteCommend(
                        id = x["id"] as String,
                        senderId = x["senderId"] as String,
                        userHardDegree = x["userHardDegree"] as Long,
                        commend = x["commend"] as String,
                        image = x["image"] as String,
                        commendTime = x["commendTime"] as String
                    )
                    list.add(routeCommendList)
                }
                _routeCommendItem.value = list
            }
        }
    }
}