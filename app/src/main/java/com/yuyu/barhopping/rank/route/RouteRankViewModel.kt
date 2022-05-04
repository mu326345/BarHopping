package com.yuyu.barhopping.rank.route

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
        db.collection("Routes").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("RouteRankViewModel", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (x in snapshot.documents) {
                    val routeList = RouteStore(
                        id = x["id"] as String,
                        startPoint = x["startPoint"] as String,
                        startLat = x["startLat"] as String,
                        startLon = x["startLon"] as String,
                        endPoint = x["endPoint"] as String,
                        endLat = x["endLat"] as String,
                        endLon = x["endLon"] as String,
                        marketCount = x["marketCount"] as Long,
                        length = x["length"] as Long,
                        hardDegree = x["hardDegree"] as Long,
                        comments = x["comments"] as String,
                        points = x["points"] as List<String>,
                        paths = x["paths"] as List<String>
                    )
                    list.add(routeList)
                }
                _routeItem.value = list
            }
        }
    }
}