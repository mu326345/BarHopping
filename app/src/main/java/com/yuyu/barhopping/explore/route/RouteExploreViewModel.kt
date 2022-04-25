package com.yuyu.barhopping.explore.route

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.RouteCommend
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class RouteExploreViewModel(repository: FirebaseRepository) : ViewModel() {

    private val _routeCommendItem = MutableLiveData<List<RouteCommend>>()
    val routeCommendItem: LiveData<List<RouteCommend>>
        get() = _routeCommendItem


    init {
        repository.getRouteDetail(object: FirebaseDataSource.RouteCallBack{
            override fun onResult(list: List<RouteCommend>) {
                _routeCommendItem.value = list
            }
        })
    }
}