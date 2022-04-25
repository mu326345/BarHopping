package com.yuyu.barhopping.repository

import com.yuyu.barhopping.data.Bar
import com.yuyu.barhopping.data.RouteCommend
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class DefaultRepository(private val dataSource: FirebaseRepository): FirebaseRepository {

    override fun getRouteDetail(callBack: FirebaseDataSource.RouteCallBack) {
        return dataSource.getRouteDetail(callBack)
    }

    override fun getBarDetail(callBack: FirebaseDataSource.BarCallBack) {
        return dataSource.getBarDetail(callBack)
    }
}