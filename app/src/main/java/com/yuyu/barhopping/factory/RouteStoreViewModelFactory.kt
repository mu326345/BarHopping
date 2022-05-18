package com.yuyu.barhopping.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuyu.barhopping.data.PointData
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.rank.route.detail.RouteDetailViewModel
import com.yuyu.barhopping.repository.FirebaseRepository

class RouteStoreViewModelFactory(
    private val routeStore: RouteStore,
    private val repository: FirebaseRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(RouteDetailViewModel::class.java) ->
                    RouteDetailViewModel(routeStore, repository)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}