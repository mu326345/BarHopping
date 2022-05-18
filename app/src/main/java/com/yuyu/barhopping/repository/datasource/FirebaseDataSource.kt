package com.yuyu.barhopping.repository.datasource

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.*
import com.yuyu.barhopping.map.MapViewModel
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.util.timeNow

class FirebaseDataSource(context: Context) : FirebaseRepository {

    private val db by lazy {
        FirebaseApp.initializeApp(context)
        Firebase.firestore
    }

    interface UserCallBack {
        fun onResult(user: User)
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

    interface UserRoutePartnerCallBack {
        fun onResult(usersPartnerList: List<Partner>)
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

    override fun snapOnRoutePartner(callBack: UserRoutePartnerCallBack, routeId: String) {
        snapOnRoutePartnersData(callBack, routeId)
    }

    override fun getOnRoutePartners(callBack: UserRoutePartnerCallBack, routeId: String) {
        getOnRoutePartnersData(callBack, routeId)
    }

    // docid == googleid
    private fun getUserData(callBack: UserCallBack, userId: String) {
        db.collection("User")
            .whereEqualTo(FieldPath.documentId(), userId)
            .get()
            .addOnSuccessListener { querySnapshot ->

                if (querySnapshot.documents.size > 0) {

                    val user = User(
                        id = querySnapshot.documents[0]["id"] as String,
                        name = querySnapshot.documents[0]["name"] as String,
                        title = querySnapshot.documents[0]["title"] as String,
                        icon = querySnapshot.documents[0]["icon"] as String,
                        routeCollection = querySnapshot.documents[0]["routeCollection"] as String,
                        userCollection = querySnapshot.documents[0]["userCollection"] as String,
                        onRoute = querySnapshot.documents[0]["onRoute"] as String?
                    )
                    callBack.onResult(user)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("yy", "Error getting documents: ", exception)
            }
    }

    private fun getRouteData(callBack: RouteCallBack, routeId: String) {
        db.collection("Routes")
            .whereEqualTo("id", routeId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("yy", "${document.id} => ${document.data}")

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
                        userIcon = document["userIcon"] as String?
                    )
                    callBack.onResult(routeList)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("yy", "Error getting documents: ", exception)
            }
    }

    private fun getUserRouteImageData(callBack: UserRouteImagesCallBack, routeId: String) {
        db.collection("Routes")
            .document(routeId)
            .collection("Images")
            .addSnapshotListener { snapshot, e ->
                val imagesList = mutableListOf<OnRouteUserImages>()

                if (e != null) {
                    Log.w(MapViewModel.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (x in snapshot.documents) {
                        val imageList = OnRouteUserImages(
                            id = x["id"] as String?:"",
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

    private fun snapOnRoutePartnersData(callBack: UserRoutePartnerCallBack, routeId: String) {
        db.collection("Routes")
            .document(routeId)
            .collection("Partners")
            .addSnapshotListener { snapshot, e ->
                val userPartnerList = mutableListOf<Partner>()

                if (e != null) {
                    Log.w(MapViewModel.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (x in snapshot.documents) {
//                        val userLocations = x.toObject(OnRouteUserLocation::class.java)
                        val partnerData = Partner(
                            id = x["id"] as String,
                            userId = x["userId"] as String,
                            lat = x["lat"] as String,
                            lng = x["lng"] as String,
                            name = x["name"] as String,
                            imageUrl = x["imageUrl"] as String,
                            finished = x["finished"] as Boolean
                        )
                        userPartnerList.add(partnerData)
                    }
                    callBack.onResult(userPartnerList)

                    Log.d(MapViewModel.TAG, "data: ${snapshot.documents}")
                } else {
                    Log.d(MapViewModel.TAG, "data: null")
                }
            }
    }

    private fun getOnRoutePartnersData(callBack: UserRoutePartnerCallBack, routeId: String) {
        db.collection("Routes")
            .document(routeId)
            .collection("Partners")
            .get().addOnSuccessListener { documents ->
                val userPartnerList = mutableListOf<Partner>()

                for (document in documents) {
                    Log.d("yy", "${document.id} => ${document.data}")

                    val partnerData = Partner(
                        id = document["id"] as String,
                        userId = document["userId"] as String,
                        lat = document["lat"] as String,
                        lng = document["lng"] as String,
                        name = document["name"] as String,
                        imageUrl = document["imageUrl"] as String,
                        finished = document["finished"] as Boolean
                    )
                    userPartnerList.add(partnerData)
                }

                callBack.onResult(userPartnerList)
            }
    }

    private fun getPointListData(callBack: PointCallBack, points: List<String>) {
        db.collection("Points")
            .whereIn("marketId", points)
            .get()
            .addOnSuccessListener { documents ->
                val unOrderList = mutableListOf<PointData>()

                for (document in documents) {
                    Log.d("yy", "${document.id} => ${document.data}")

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
                Log.w("yy", "Error getting documents: ", exception)
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