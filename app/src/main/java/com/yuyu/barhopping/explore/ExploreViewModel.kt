package com.yuyu.barhopping.explore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.CommonField
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.BarPost
import com.yuyu.barhopping.data.NewBarPost
import com.yuyu.barhopping.data.NewRouteStore
import com.yuyu.barhopping.rank.route.RouteRankViewModel
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class ExploreViewModel(val repository: FirebaseRepository?) : ViewModel() {

    private val db = Firebase.firestore

    private val _barItem = MutableLiveData<List<BarPost>>()
    val barItem: LiveData<List<BarPost>>
        get() = _barItem

    private val _newBarItem = MutableLiveData<List<NewBarPost>>()
    val newBarItem: LiveData<List<NewBarPost>>
        get() = _newBarItem

    private val _collectionList = MutableLiveData<List<String>>()
    val collectionList: LiveData<List<String>>
        get() = _collectionList

    private val _navigateToProgress = MutableLiveData<Boolean>()
    val navigateToProgress: LiveData<Boolean>
        get() = _navigateToProgress

    init {
        getBarDetail()
        getUserCollection()
    }

    private fun getBarDetail() {
        repository?.getBarDetail(object : FirebaseDataSource.BarCallBack {
            override fun onResult(list: List<BarPost>) {
                _barItem.value = list
            }
        })
    }

    private fun getUserCollection() {
        UserManager.user?.id?.let {
            db.collection(CommonField.USER)
                .document(it)
                .collection("barCollection")
                .get()
                .addOnSuccessListener { document ->
                    val collection = mutableListOf<String>()

                    if (document != null) {
                        for (doc in document) {
                            collection.add(doc.id)
                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                    _collectionList.value = collection
                    checkUserCollection()
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    private fun checkUserCollection() {
        val list = mutableListOf<NewBarPost>()
        barItem.value?.forEach { bar ->
            var canAdd = false
            collectionList.value?.forEach { collectId ->
                if (bar.id == collectId) {
                    canAdd = true
                }
            }
            if (canAdd) {
                val newBar = NewBarPost(
                    bar.id, bar.userId, bar.userName, bar.userImg, bar.uri, bar.commend,
                    bar.barName, bar.barId, bar.barAddress, bar.barLat, bar.barLng, bar.barPhone,
                    bar.time, true
                )
                list.add(newBar)
            } else {
                val newBar = NewBarPost(
                    bar.id, bar.userId, bar.userName, bar.userImg, bar.uri, bar.commend,
                    bar.barName, bar.barId, bar.barAddress, bar.barLat, bar.barLng, bar.barPhone,
                    bar.time, false
                )
                list.add(newBar)
            }
        }
        _newBarItem.value = list
    }


    fun barCollection(barId: String, isLike: Boolean) {
        if (!isLike) {
            // 未收藏，可以收藏
            uploadBarCollection(barId)
        } else {
            // 已收藏，取消收藏
            deleteBarCollection(barId)
        }
    }

    private fun uploadBarCollection(barId: String) {
        UserManager.user?.id?.let {
            val id = HashMap<String, String>()
            id[CommonField.BAR_ID] = barId

            db.collection(CommonField.USER)
                .document(it)
                .collection(CommonField.BAR_COLLECTION)
                .document(barId)
                .set(id)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                }
        }
        getUserCollection()
    }


    private fun deleteBarCollection(barId: String) {
        UserManager.user?.id?.let {
            db.collection(CommonField.USER)
                .document(it)
                .collection(CommonField.BAR_COLLECTION)
                .document(barId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        }
        getUserCollection()
    }

    fun navigateToProgress() {
        _navigateToProgress.value = true
    }

    fun onNavigateProgress() {
        _navigateToProgress.value = false
    }

    companion object {
        private val TAG = ExploreViewModel::class.java.simpleName
    }
}