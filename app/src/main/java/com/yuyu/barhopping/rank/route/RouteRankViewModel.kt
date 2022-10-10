package com.yuyu.barhopping.rank.route

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.CommonField
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.NewRouteStore
import com.yuyu.barhopping.data.RouteStore

class RouteRankViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _routeItem = MutableLiveData<List<RouteStore>>()
    val routeItem: LiveData<List<RouteStore>>
        get() = _routeItem

    private val _newRouteItem = MutableLiveData<List<NewRouteStore>>()
    val newRouteItem: LiveData<List<NewRouteStore>>
        get() = _newRouteItem

    private val _navigateToDetail = MutableLiveData<NewRouteStore>()
    val navigateToDetail: LiveData<NewRouteStore>
        get() = _navigateToDetail

    private val _collectionList = MutableLiveData<List<String>>()
    val collectionList: LiveData<List<String>>
        get() = _collectionList

    private val _navigateToProgress = MutableLiveData<Boolean>()
    val navigateToProgress: LiveData<Boolean>
        get() = _navigateToProgress

    init {
        snapRouteData()
        getUserCollection()
    }

    private fun getUserCollection() {
        UserManager.user?.id?.let {
            db.collection(CommonField.USER)
                .document(it)
                .collection("routeCollection")
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

    private fun snapRouteData() {
        val list = mutableListOf<RouteStore>()
        val docRef = db.collection(CommonField.ROUTES)
        docRef.orderBy(CommonField.TIME, Query.Direction.DESCENDING).limit(10)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("RouteRankViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (x in snapshot.documents) {
                        val routeList = RouteStore(
                            id = x["id"] as String,
                            startPoint = x["startPoint"] as String,
                            startLat = x["startLat"] as String,
                            startLon = x["startLon"] as String,
                            endPoint = x["endPoint"] as String,
                            endLat = x["endLat"] as String,
                            endLon = x["endLon"] as String,
                            marketCount = x["marketCount"] as Long,
                            length = x["length"] as Long,
                            hardDegree = x["hardDegree"] as Long?,
                            comments = x["comments"] as String?,
                            points = x["points"] as List<String>?,
                            paths = x["paths"] as List<String>?,
                            time = x["time"] as String?,
                            userName = x["userName"] as String?,
                            userIcon = x["userIcon"] as String?,
                            userId = x["userId"] as String?
                        )
                        list.add(routeList)
                    }
                    _routeItem.value = list
                }
            }
    }

    private fun checkUserCollection() {
        val list = mutableListOf<NewRouteStore>()
        routeItem.value?.forEach { route ->
            var canAdd = false
            collectionList.value?.forEach { collectId ->
                if (route.id == collectId) {
                    canAdd = true
                }
            }
            if (canAdd) {
                val newRoute = NewRouteStore(
                    route.id, route.startPoint, route.startLat, route.startLon,
                    route.endPoint, route.endLat, route.endLon,
                    route.marketCount, route.length, route.hardDegree, route.comments,
                    route.points, route.paths, route.time,
                    route.userName, route.userIcon, route.userId,
                    true
                )
                list.add(newRoute)
            } else {
                val newRoute = NewRouteStore(
                    route.id, route.startPoint, route.startLat, route.startLon,
                    route.endPoint, route.endLat, route.endLon,
                    route.marketCount, route.length, route.hardDegree, route.comments,
                    route.points, route.paths, route.time,
                    route.userName, route.userIcon, route.userId,
                    false
                )
                list.add(newRoute)
            }
        }
        _newRouteItem.value = list
    }

    fun routeCollection(routeId: String, isLike: Boolean) {
        if(!isLike) {
            // 未收藏，可以收藏
            uploadRouteCollection(routeId)
        } else {
            // 已收藏，取消收藏
            deleteRouteCollection(routeId)
        }
    }

    private fun uploadRouteCollection(routeId: String) {
        UserManager.user?.id?.let {
            val id = HashMap<String, String>()
            id[CommonField.ROUTE_ID] = routeId

            db.collection(CommonField.USER)
                .document(it)
                .collection(CommonField.ROUTE_COLLECTION)
                .document(routeId)
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

    private fun deleteRouteCollection(routeId: String) {
        UserManager.user?.id?.let {
            db.collection(CommonField.USER)
                .document(it)
                .collection(CommonField.ROUTE_COLLECTION)
                .document(routeId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        }
        getUserCollection()
    }

    fun navigateToDetail(routeStore: NewRouteStore) {
        _navigateToDetail.value = routeStore
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }

    fun navigateToProgress() {
        _navigateToProgress.value = true
    }

    fun onNavigateProgress() {
        _navigateToProgress.value = false
    }

    companion object {
        private val TAG = RouteRankViewModel::class.java.simpleName
    }
}