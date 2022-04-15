package com.yuyu.barhopping.rank.route

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.Route
import com.yuyu.barhopping.data.RouteStore

class RouteRankViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _routeItem = MutableLiveData<List<RouteStore>>()
    val routeItem: LiveData<List<RouteStore>>
        get() = _routeItem

    private val list = mutableListOf<RouteStore>()

    init {
        snapRouteData()
    }

    private fun snapRouteData() {
        db.collection("Route").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (x in snapshot.documents) {
                    val routeList = RouteStore(
                        id = x["id"] as String,
                        startPoint = x["startPoint"] as String,
                        endPoint = x["endPoint"] as String,
                        points = x["points"] as String,
                        comments = x["comments"] as String,
                        hardDegree = x["hardDegree"] as Long,
                        length = x["length"] as Long
                    )
                    list.add(routeList)
                }
                _routeItem.value = list
            }
        }
    }
}