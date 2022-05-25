package com.yuyu.barhopping.rank.person

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.data.Route
import com.yuyu.barhopping.data.User

class PersonRankViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _userItem = MutableLiveData<List<User>>()
    val userItem: LiveData<List<User>>
        get() = _userItem

    private val list = mutableListOf<User>()

    init {
        snapUserData()
    }

    private fun snapUserData() {
        db.collection("User").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if(snapshot != null) {
                for(x in snapshot.documents) {
                    val userList = User(
                        id = x["id"] as String,
                        name = x["name"] as String,
                        title = x["title"] as String,
                        icon = x["icon"] as String,
                        routeCollection = x["routeCollection"] as String,
                        userCollection = x["userCollection"] as String,
                        onRoute = x["onRoute"] as String?,
                        barPost = x["barPost"] as List<String>?,
                        marketCount = x["marketCount"] as List<Int>?
                    )
                    list.add(userList)
                }
                _userItem.value = list
            }
        }
    }
}