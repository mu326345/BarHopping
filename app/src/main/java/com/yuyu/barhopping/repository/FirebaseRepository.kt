package com.yuyu.barhopping.repository

import com.yuyu.barhopping.data.Bar
import com.yuyu.barhopping.data.RouteCommend
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

interface FirebaseRepository {

    fun getRouteDetail(callBack: FirebaseDataSource.RouteCallBack)

    fun getBarDetail(callBack: FirebaseDataSource.BarCallBack)
}