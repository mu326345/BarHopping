package com.yuyu.barhopping.repository

import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

interface FirebaseRepository {

    fun getRouteCommendDetail(callBack: FirebaseDataSource.RouteCommendCallBack)

    fun getBarDetail(callBack: FirebaseDataSource.BarCallBack)

    fun getRouteDetail(callBack: FirebaseDataSource.RouteCallBack, routeId: String)

    fun getPointDetailList(callBack: FirebaseDataSource.PointCallBack, points: List<String>)

    fun getUserRouteImages(callBack: FirebaseDataSource.UserRouteImagesCallBack, routeId: String)

    fun snapOnRoutePartner(callBack: FirebaseDataSource.UserRoutePartnerCallBack, routeId: String)

    fun getOnRoutePartners(callBack: FirebaseDataSource.UserRoutePartnerCallBack, routeId: String)

    fun getUserDetail(callBack: FirebaseDataSource.UserCallBack, userId: String)
}