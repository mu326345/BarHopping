package com.yuyu.barhopping.map.sheet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.RouteStore

class BottomSheetViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _routeDataList = MutableLiveData<RouteStore>()
    val routeDataList: LiveData<RouteStore>
        get() = _routeDataList

    private val _marketName = MutableLiveData<List<String>>()
    val marketName: LiveData<List<String>>
        get() = _marketName

    // user for search for point market name
    val nameList = Transformations.map(routeDataList) {
        it.points
    }

    var idStr = "vaFVZgqwkmW367bVB32A"

    init {
        idStr = "vaFVZgqwkmW367bVB32A"
        getRouteData()
    }

    fun getRouteData() {
        db.collection("Route")
            .whereEqualTo("id", idStr)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")

                    val routeList = RouteStore(
                        id = document["id"] as String,
                        startPoint = document["startPoint"] as String,
                        startLat = document["startLat"] as String,
                        startLon = document["startLon"] as String,
                        endPoint = document["endPoint"] as String,
                        endLat = document["endLat"] as String,
                        endLon = document["endLon"] as String,
                        marketCount = document["marketCount"] as Long,
                        length = document["length"] as Long,
                        hardDegree = document["hardDegree"] as Long,
                        comments = document["comments"] as String,
                        points = document["points"] as ArrayList<String>
                    )
                    _routeDataList.value = routeList
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun findPointName() {
        val list = mutableListOf<String>()
        nameList.value?.let {
            it.forEach {
                db.collection("Point")
                    .whereEqualTo("marketId", it)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                            val name = document["name"]
                            list.add(name.toString())
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }
            }
            _marketName.value = list
        }
    }

    companion object {
        val TAG = "BottomSheetViewModel"
    }
}