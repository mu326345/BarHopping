package com.yuyu.barhopping.repository.datasource

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.CommonField
import com.yuyu.barhopping.data.*
import com.yuyu.barhopping.map.MapViewModel
import com.yuyu.barhopping.repository.FirebaseRepository

class FirebaseDataSource(context: Context) : FirebaseRepository {

    private val db by lazy {
        FirebaseApp.initializeApp(context)
        Firebase.firestore
    }

    interface UserCallBack {
        fun onResult(user: User)
    }

    interface BarCallBack {
        fun onResult(list: List<BarPost>)
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

    private fun getUserData(callBack: UserCallBack, userId: String) {
        db.collection(CommonField.USER)
            .whereEqualTo(FieldPath.documentId(), userId)
            .get()
            .addOnSuccessListener { userData ->

                if (userData.documents.size > 0) {
                    val user = userData.toObjects(User::class.java)[0]

                    callBack.onResult(user)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("yy", "Error getting documents: ", exception)
            }
    }

    private fun getRouteData(callBack: RouteCallBack, routeId: String) {
        db.collection(CommonField.ROUTES)
            .whereEqualTo(CommonField.ROUTE_ID, routeId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("yy", "${document.id} => ${document.data}")

                    val routeList = document.toObject(RouteStore::class.java)

                    callBack.onResult(routeList)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("yy", "Error getting documents: ", exception)
            }
    }

    private fun getUserRouteImageData(callBack: UserRouteImagesCallBack, routeId: String) {
        db.collection(CommonField.ROUTES)
            .document(routeId)
            .collection(CommonField.IMAGES)
            .addSnapshotListener { snapshot, e ->
                val imagesList = mutableListOf<OnRouteUserImages>()

                if (e != null) {
                    Log.w(MapViewModel.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (x in snapshot.documents) {
                        val imageList = x.toObject(OnRouteUserImages::class.java)

                        if (imageList != null) {
                            imagesList.add(imageList)
                        }
                    }
                    callBack.onResult(imagesList)

                    Log.d(MapViewModel.TAG, "data: ${snapshot.documents}")
                } else {
                    Log.d(MapViewModel.TAG, "data: null")
                }
            }
    }

    private fun snapOnRoutePartnersData(callBack: UserRoutePartnerCallBack, routeId: String) {
        db.collection(CommonField.ROUTES)
            .document(routeId)
            .collection(CommonField.PARTNERS)
            .addSnapshotListener { snapshot, e ->
                val userPartnerList = mutableListOf<Partner>()

                if (e != null) {
                    Log.w(MapViewModel.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (x in snapshot.documents) {
                        val partnerData = x.toObject(Partner::class.java)

                        if (partnerData != null) {
                            userPartnerList.add(partnerData)
                        }
                    }
                    callBack.onResult(userPartnerList)

                    Log.d(MapViewModel.TAG, "data: ${snapshot.documents}")
                } else {
                    Log.d(MapViewModel.TAG, "data: null")
                }
            }
    }

    private fun getOnRoutePartnersData(callBack: UserRoutePartnerCallBack, routeId: String) {
        db.collection(CommonField.ROUTES)
            .document(routeId)
            .collection(CommonField.PARTNERS)
            .get()
            .addOnSuccessListener { documents ->
                val userPartnerList = mutableListOf<Partner>()

                for (document in documents) {
                    Log.d("yy", "${document.id} => ${document.data}")

                    val partnerData = document.toObject(Partner::class.java)
                    userPartnerList.add(partnerData)
                }

                callBack.onResult(userPartnerList)
            }
    }

    private fun getPointListData(callBack: PointCallBack, points: List<String>) {
        db.collection(CommonField.POINTS)
            .whereIn(CommonField.MARKET_ID, points)
            .get()
            .addOnSuccessListener { documents ->
                val unOrderList = mutableListOf<PointData>()

                for (document in documents) {
                    Log.d("yy", "${document.id} => ${document.data}")

                    val pointList = document.toObject(PointData::class.java)
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
        val docRef = db.collection(CommonField.BAR_POST)
        docRef.orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
            val list = mutableListOf<BarPost>()

            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (x in snapshot.documents) {
                    val barList = x.toObject(BarPost::class.java)

                    if (barList != null) {
                        list.add(barList)
                    }
                }
                callBack.onResult(list)
            }
        }
    }

    private fun snapRouteCommendData(callBack: RouteCommendCallBack) {
        db.collection(CommonField.ROUTE_COMMEND).addSnapshotListener { snapshot, e ->
            val list = mutableListOf<RouteCommend>()
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (x in snapshot.documents) {
                    val routeCommendList = x.toObject(RouteCommend::class.java)

                    if (routeCommendList != null) {
                        list.add(routeCommendList)
                    }
                }
                callBack.onResult(list)
            }
        }
    }
}