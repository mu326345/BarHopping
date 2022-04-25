package com.yuyu.barhopping.explore.bar

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.Bar
import com.yuyu.barhopping.data.RouteCommend
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource
import kotlin.math.absoluteValue

class BarExploreViewModel(repository: FirebaseRepository) : ViewModel() {

    private val _barItem = MutableLiveData<List<Bar>>()
    val barItem: LiveData<List<Bar>>
        get() = _barItem


    init {
        repository.getBarDetail(object: FirebaseDataSource.BarCallBack{
            override fun onResult(list: List<Bar>) {
                _barItem.value = list
            }
        })
    }
}