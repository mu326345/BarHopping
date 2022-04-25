package com.yuyu.barhopping.explore.grid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yuyu.barhopping.repository.FirebaseRepository

class GridExploreViewModel(repository: FirebaseRepository) : ViewModel() {

    // Triple<image, commend time, like>
    private val _imagesList = MutableLiveData<List<Triple<String,String,Long>>>()
    val imagesList: LiveData<List<Triple<String, String, Long>>>
    get() = _imagesList
}