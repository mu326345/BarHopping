package com.yuyu.barhopping.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuyu.barhopping.explore.ExploreViewModel
import com.yuyu.barhopping.login.GoogleLoginViewModel
import com.yuyu.barhopping.map.MapViewModel
import com.yuyu.barhopping.profile.ProfileViewModel
import com.yuyu.barhopping.repository.FirebaseRepository

class ViewModelFactory(val repository: FirebaseRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        with(modelClass) {
            when {
                isAssignableFrom(ExploreViewModel::class.java) ->
                    ExploreViewModel(repository)

                isAssignableFrom(MapViewModel::class.java) ->
                    MapViewModel(repository)

                isAssignableFrom(GoogleLoginViewModel::class.java) ->
                    GoogleLoginViewModel(repository)

                isAssignableFrom(ProfileViewModel::class.java) ->
                    ProfileViewModel(repository)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}