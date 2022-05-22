package com.yuyu.barhopping.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.BarPost
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.repository.FirebaseRepository

class ProfileViewModel(val repository: FirebaseRepository) : ViewModel() {

    private val db = Firebase.firestore

    var userRouteHistory: List<RouteStore> ?= null

//    var userMarketHistory: Long = 0
    private val _userMarketHistory = MutableLiveData<Long>()
    val userMarketHistory: LiveData<Long>
        get() = _userMarketHistory

    var userBarShareHistory: List<BarPost> ?= null

//    var userBarShareCount: Int ?= 0
    private val _userBarShareCount = MutableLiveData<Int>()
    val userBarShareCount: LiveData<Int>
        get() = _userBarShareCount

    init {
        getUserRouteHistory()
        userBarShare()
    }

    private fun getUserRouteHistory() {
        db.collection("Routes")
            .whereEqualTo("userId", UserManager.user?.id)
            .get()
            .addOnSuccessListener { documents ->
                val list = mutableListOf<RouteStore>()
                for (document in documents) {
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
                        hardDegree = document["hardDegree"] as Int?,
                        comments = document["comments"] as String?,
                        points = document["points"] as List<String>?,
                        paths = document["paths"] as List<String>?,
                        time = document["time"] as String,
                        userName = document["userName"] as String?,
                        userIcon = document["userIcon"] as String?,
                        userId = document["userId"] as String
                    )
                    list.add(routeList)
                }
                userRouteHistory = list
                userMarketHistory()
            }
            .addOnFailureListener {
                Log.w("yy", "Error getting documents: ")
            }
    }

    private fun userMarketHistory() {
        userRouteHistory?.let {
            var count: Long = 0
            it.forEach {
                count += it.marketCount
            }
            _userMarketHistory.value = count
        }
    }

    private fun userBarShare() {
        db.collection("BarPost")
            .whereEqualTo("userId", UserManager.user?.id)
            .get()
            .addOnSuccessListener { documents ->
                val list = mutableListOf<BarPost>()
                for (document in documents) {
                    val barData = BarPost(
                        id = document["id"] as String,
                        userId = document["userId"] as String,
                        userName = document["userName"] as String,
                        userImg = document["userImg"] as String,
                        uri = document["uri"] as String,
                        commend = document["commend"] as String,
                        barName = document["barName"] as String,
                        barId = document["barId"] as String,
                        barAddress = document["barAddress"] as String?,
                        barLat = document["barLat"] as String,
                        barLng = document["barLng"] as String,
                        barPhone = document["barPhone"] as String?,
                        time = document["time"] as String,
                    )
                    list.add(barData)
                }
                userBarShareHistory = list
                _userBarShareCount.value = (userBarShareHistory as MutableList<BarPost>).size
            }
            .addOnFailureListener {
                Log.w("yy", "Error getting documents: ")
            }
    }

}