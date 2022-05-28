package com.yuyu.barhopping.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yuyu.barhopping.data.BarPost
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class ExploreViewModel(val repository: FirebaseRepository) : ViewModel() {

    private val _barItem = MutableLiveData<List<BarPost>>()
    val barItem: LiveData<List<BarPost>>
        get() = _barItem

    init {
        getBarDetail()
    }

    fun getBarDetail() {
        repository.getBarDetail(object: FirebaseDataSource.BarCallBack{
            override fun onResult(list: List<BarPost>) {
                _barItem.value = list
            }
        })
    }
}