package com.yuyu.barhopping

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yuyu.barhopping.util.CurrentFragmentType

class MainViewModel: ViewModel() {

    // Record current fragment to support data binding
    val currentFragmentType = MutableLiveData<CurrentFragmentType>()
}