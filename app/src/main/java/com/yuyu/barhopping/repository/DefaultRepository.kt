package com.yuyu.barhopping.repository

import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class DefaultRepository(private val dataSource: FirebaseRepository): FirebaseRepository {

    override fun getRouteCommendDetail(callBack: FirebaseDataSource.RouteCommendCallBack) {
        return dataSource.getRouteCommendDetail(callBack)
    }

    override fun getBarDetail(callBack: FirebaseDataSource.BarCallBack) {
        return dataSource.getBarDetail(callBack)
    }

    override fun getRouteDetail(callBack: FirebaseDataSource.RouteCallBack, routeId: String) {
        return dataSource.getRouteDetail(callBack, routeId)
    }

    override fun getPointDetailList(callBack: FirebaseDataSource.PointCallBack, points: List<String>) {
        return dataSource.getPointDetailList(callBack, points)
    }

    override fun getUserRouteImages(callBack: FirebaseDataSource.UserRouteImagesCallBack, routeId: String) {
        return dataSource.getUserRouteImages(callBack, routeId)
    }

    override fun snapOnRoutePartner(callBack: FirebaseDataSource.UserRoutePartnerCallBack, routeId: String) {
        return dataSource.snapOnRoutePartner(callBack, routeId)
    }

    override fun getUserDetail(callBack: FirebaseDataSource.UserCallBack, userId: String) {
        return dataSource.getUserDetail(callBack, userId)
    }
}