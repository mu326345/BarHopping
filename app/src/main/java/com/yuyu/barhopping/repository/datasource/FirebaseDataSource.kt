package com.yuyu.barhopping.repository.datasource

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
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

    interface UserCallBack {
        fun onRoute(user: List<User>)
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
        fun onResult(list: List<PointData>)
    }

    interface UserRouteImagesCallBack {
        fun onResult(imageList: List<OnRouteUserImages>)
    }

    interface UserRouteLocationCallBack {
        fun onResult(usersLocationList: List<OnRouteUserLocation>)
    }

    override fun getUserDetail(callBack: UserCallBack, userId: String) {
        getUserData(callBack, userId)
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

    override fun getOnRouteUserLocation(callBack: UserRouteLocationCallBack, routeId: String) {
        getOnRouteUserLocationData(callBack, routeId)
    }

    private fun getUserData(callBack: UserCallBack, userId: String) {
        val userList = mutableListOf<User>()
        db.collection("User")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(BottomSheetViewModel.TAG, "${document.id} => ${document.data}")

                    val user = User(
                        id = document["id"] as String,
                        name = document["name"] as String,
                        title = document["title"] as String,
                        icon = document["icon"] as String,
                        routeCollection = document["routeCollection"] as String,
                        userCollection = document["userCollection"] as String,
                        onRoute = document["onRoute"] as String
                    )
                    userList.add(user)
                }
                callBack.onRoute(userList)
            }
            .addOnFailureListener { exception ->
                Log.w(BottomSheetViewModel.TAG, "Error getting documents: ", exception)
            }
    }

    private fun getRouteData(callBack: RouteCallBack, routeId: String) {
        db.collection("Routes")
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
                        hardDegree = document["hardDegree"] as Long?,
                        comments = document["comments"] as String?,
                        points = document["points"] as List<String>,
                        paths = document["paths"] as List<String>
                    )
                    callBack.onResult(routeList)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(BottomSheetViewModel.TAG, "Error getting documents: ", exception)
            }
    }

    private fun getUserRouteImageData(callBack: UserRouteImagesCallBack, routeId: String) {
        db.collection("Routes")
            .document(routeId)
            .collection("images")
            .addSnapshotListener { snapshot, e ->
                val imagesList = mutableListOf<OnRouteUserImages>()
                
                if (e != null) {
                    Log.w(MapViewModel.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (x in snapshot.documents) {
                        val imageList = OnRouteUserImages(
                            id = x["id"] as String,
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

    private fun getOnRouteUserLocationData(callBack: UserRouteLocationCallBack, routeId: String) {
        val userLocationList = mutableListOf<OnRouteUserLocation>()
        db.collection("Routes")
            .document(routeId)
            .collection("locations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(MapViewModel.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (x in snapshot.documents) {
//                        val userLocations = x.toObject(OnRouteUserLocation::class.java)
                        val userLocations = OnRouteUserLocation(
                            id = x["id"] as String,
                            userId = x["userId"] as String,
                            lat = x["lat"] as String,
                            lng = x["lng"] as String
                        )
                        userLocationList.add(userLocations)
                    }
                    callBack.onResult(userLocationList)

                    Log.d(MapViewModel.TAG, "data: ${snapshot.documents}")
                } else {
                    Log.d(MapViewModel.TAG, "data: null")
                }
            }
    }

    private fun getPointListData(callBack: PointCallBack, points: List<String>) {
        val unOrderList = mutableListOf<PointData>()
        db.collection("Points")
            .whereIn("marketId", points)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(BottomSheetViewModel.TAG, "${document.id} => ${document.data}")

                    val pointList = PointData(
                        latitude = document["latitude"].toString().toDouble(),
                        longitude = document["longitude"].toString().toDouble(),
                        marketId = document["marketId"] as String,
                        name = document["name"] as String,
                        type = document["type"].toString().toInt()
                    )
                    unOrderList.add(pointList)
                }

                val orderPointList = mutableListOf<PointData>()
                points.forEach { placeId ->
                    unOrderList.forEach {
                        if (placeId == it.marketId) {
                            orderPointList.add(it)
                        }
                    }
                }
                callBack.onResult(orderPointList)
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