package com.yuyu.barhopping.rank.person

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.CommonField
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
        val docRef = db.collection(CommonField.USER)
        docRef.orderBy("marketCount", Query.Direction.DESCENDING).orderBy("barPost", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ContentValues.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (x in snapshot.documents) {
                        val userList = x.toObject(User::class.java)

                        if (userList != null) {
                            list.add(userList)
                        }
                    }
                    _userItem.value = list
                }
            }
    }
}