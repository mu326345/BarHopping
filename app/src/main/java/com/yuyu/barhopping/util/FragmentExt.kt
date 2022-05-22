package com.yuyu.barhopping.util

import androidx.fragment.app.Fragment
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.data.NewRouteStore
import com.yuyu.barhopping.data.PointData
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.factory.RouteStoreViewModelFactory

fun Fragment.getVmFactory(newRouteStore: NewRouteStore): RouteStoreViewModelFactory {
    val repository = (requireContext().applicationContext as Application).repository
    return RouteStoreViewModelFactory(newRouteStore, repository)
}