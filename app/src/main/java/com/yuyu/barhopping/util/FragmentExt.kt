package com.yuyu.barhopping.util

import androidx.fragment.app.Fragment
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.data.PointData
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.factory.RouteStoreViewModelFactory

fun Fragment.getVmFactory(routeStore: RouteStore): RouteStoreViewModelFactory {
    val repository = (requireContext().applicationContext as Application).repository
    return RouteStoreViewModelFactory(routeStore, repository)
}