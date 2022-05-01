package com.yuyu.barhopping.repository.datasource

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.*
import com.yuyu.barhopping.map.MapViewModel
import com.yuyu.barhopping.map.sheet.BottomSheetViewModel
import com.yuyu.barhopping.repository.FirebaseRepository

class FirebaseDataSource(context: Context) : FirebaseRepository {

    private val db by lazy {
        FirebaseApp.initializeApp(context)
        Firebase.firestore
    }

    interface BarCallBack {
        fun onResult(list: List<Bar>)
    }

    interface RouteCommendCallBack {
        fun onResult(list: List<RouteCommend>)
    }

    interface RouteCallBack {
        fun onResult(list: RouteStore)
    }

    interface PointCallBack {
        fun onResult(list: List<Point>)
    }

    interface UserRouteImagesCallBack {
        fun onResult(imageList: List<PointImages>)
    }

    override fun getRouteCommendDetail(callBack: RouteCommendCallBack) {
        snapRouteCommendData(callBack)
    }

    override fun getBarDetail(callBack: BarCallBack) {
        snapBarData(callBack)
    }

    override fun getRouteDetail(callBack: RouteCallBack, routeId: String) {
        getRouteData(callBack, routeId)
    }

    override fun getPointDetailList(callBack: PointCallBack, points: List<String>) {
        getPointListData(callBack, points)
    }

    override fun getUserRouteImages(callBack: UserRouteImagesCallBack, routeId: String) {
        getUserRouteImageData(callBack, routeId)
    }


    private fun getRouteData(callBack: RouteCallBack, routeId: String) {
        db.collection("Route")
            .whereEqualTo("id", routeId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(BottomSheetViewModel.TAG, "${document.id} => ${document.data}")

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
                        points = document["points"] as List<String>
                    )
                    callBack.onResult(routeList)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(BottomSheetViewModel.TAG, "Error getting documents: ", exception)
            }
    }

    private fun getUserRouteImageData(callBack: UserRouteImagesCallBack, routeId: String) {
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
                            id = x["id"].toString(),
                            pointId = x["pointId"] as String,
                            url = x["url"] as String,
                            userId = x["userId"] as String
                        )
                        imagesList.add(imageList)
                    }
                    callBack.onResult(imagesList)

                    Log.d(MapViewModel.TAG, "data: ${snapshot.documents}")
                } else {
                    Log.d(MapViewModel.TAG, "data: null")
                }
            }
    }

    private fun getPointListData(callBack: PointCallBack, points: List<String>) {
        val list = mutableListOf<Point>()
        db.collection("Point")
            .whereIn("marketId", points)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(BottomSheetViewModel.TAG, "${document.id} => ${document.data}")

                    val pointList = Point(
                        latitude = document["latitude"].toString().toDouble(),
                        longitude = document["longitude"].toString().toDouble(),
                        marketId = document["marketId"] as String,
                        name = document["name"] as String,
                        type = document["type"].toString().toInt()
                    )
                    list.add(pointList)
                }
                callBack.onResult(list)
            }
            .addOnFailureListener { exception ->
                Log.w(BottomSheetViewModel.TAG, "Error getting documents: ", exception)
            }
    }

    private fun snapBarData(callBack: BarCallBack) {
        db.collection("Bar").addSnapshotListener { snapshot, e ->
            val list = mutableListOf<Bar>()
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (x in snapshot.documents) {
                    val barList = Bar(
                        id = x["id"] as String,
                        senderId = x["senderId"] as String,
                        locationX = x.getDouble("locationX") as Double,
                        locationY = x.getDouble("locationY") as Double,
                        name = x["name"] as String,
                        address = x["address"] as String,
                        image = x["image"] as String,
                        commend = x["commend"] as String,
                        phone = x["phone"] as String,
                        like = x["like"] as Long
                    )
                    list.add(barList)
                }
                callBack.onResult(list)
            }
        }
    }

    private fun snapRouteCommendData(callBack: RouteCommendCallBack) {
        db.collection("RouteCommend").addSnapshotListener { snapshot, e ->
            val list = mutableListOf<RouteCommend>()
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
                        commendTime = x["commendTime"] as String,
                        like = x["like"] as Long
                    )
                    list.add(routeCommendList)
                }
                callBack.onResult(list)
            }
        }
    }
}