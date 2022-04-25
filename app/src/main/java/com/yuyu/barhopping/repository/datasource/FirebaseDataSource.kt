package com.yuyu.barhopping.repository.datasource

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.Bar
import com.yuyu.barhopping.data.RouteCommend
import com.yuyu.barhopping.repository.FirebaseRepository

class FirebaseDataSource(context: Context) : FirebaseRepository {

    private val db by lazy {
    FirebaseApp.initializeApp(context)
    Firebase.firestore
    }

    interface BarCallBack {
        fun onResult(list: List<Bar>)
    }
    interface RouteCallBack {
        fun onResult(list: List<RouteCommend>)
    }

    override fun getRouteDetail(callBack: RouteCallBack) {
        snapRouteCommendData(callBack)
    }

    override fun getBarDetail(callBack: BarCallBack) {
        snapBarData(callBack)
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

    private fun snapRouteCommendData(callBack: RouteCallBack) {
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