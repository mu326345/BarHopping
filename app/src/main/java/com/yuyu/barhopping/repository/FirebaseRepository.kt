package com.yuyu.barhopping.repository

import com.yuyu.barhopping.data.Point
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

interface FirebaseRepository {

    fun getRouteCommendDetail(callBack: FirebaseDataSource.RouteCommendCallBack)

    fun getBarDetail(callBack: FirebaseDataSource.BarCallBack)

    fun getRouteDetail(callBack: FirebaseDataSource.RouteCallBack, routeId: String)

    fun getPointDetailList(callBack: FirebaseDataSource.PointCallBack, points: List<String>)

    fun getUserRouteImages(callBack: FirebaseDataSource.UserRouteImagesCallBack, routeId: String)
}