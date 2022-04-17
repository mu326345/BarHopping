package com.yuyu.barhopping.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuyu.barhopping.explore.bar.BarExploreViewModel
import com.yuyu.barhopping.explore.route.RouteExploreViewModel
import com.yuyu.barhopping.repository.FirebaseRepository

class ViewModelFactory(val repository: FirebaseRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(RouteExploreViewModel::class.java) ->
                    RouteExploreViewModel(repository)

                isAssignableFrom(BarExploreViewModel::class.java) ->
                    BarExploreViewModel(repository)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}