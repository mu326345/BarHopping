package com.yuyu.barhopping.map.sheet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.PointImages
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.map.MapViewModel

class BottomSheetViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _routeDataList = MutableLiveData<RouteStore>()
    val routeDataList: LiveData<RouteStore>
        get() = _routeDataList

    private val _marketName = MutableLiveData<List<String>>()
    val marketName: LiveData<List<String>>
        get() = _marketName

    // user search for point market name
    val pointsList = Transformations.map(routeDataList) {
        it.points
    }

    private val _imagesLiveData = MutableLiveData<List<PointImages>>()
    val imagesLiveData: LiveData<List<PointImages>>
        get() = _imagesLiveData

    private val _finishedGame = MutableLiveData<Boolean>()
    val finishedGame: LiveData<Boolean>
        get() = _finishedGame


    var idStr = "vaFVZgqwkmW367bVB32A"
    var userId = "yuyu11111"

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
        pointsList.value?.let {
            db.collection("Point")
                .whereIn("marketId", it)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        val name = document["name"]
                        list.add(name.toString())
                    }
                    _marketName.value = list
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    fun getUserRouteImages(routeId: String) {
        val imagesList = mutableListOf<PointImages>()
        db.collection("Route")
            .document(routeId)
            .collection("image")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(MapViewModel.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (x in snapshot.documents) {
                        val imageList = PointImages(
                            id = x["id"] as String,
                            pointId = x["pointId"] as String,
                            url = x["url"] as String,
                            userId = x["userId"] as String
                        )
                        imagesList.add(imageList)
                    }
                    _imagesLiveData.value = imagesList
                    Log.d(MapViewModel.TAG, "data: ${snapshot.documents}")
                } else {
                    Log.d(MapViewModel.TAG, "data: null")
                }
            }
    }

    // in all images find user then check
    fun checkUserFinished() {
        var count = 0
        imagesLiveData.value?.forEach {
            if (it.userId == userId) {
                count++
            }
        }

        pointsList.value?.size.let {
            if (count == it) {
                Log.v("QAQ", it.toString())
                _finishedGame.value = true
            }
        }
    }

    fun finishedGameToNull() {
        _finishedGame.value = null
    }

    companion object {
        val TAG = "BottomSheetViewModel"
    }
}