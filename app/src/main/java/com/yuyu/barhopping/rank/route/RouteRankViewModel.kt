package com.yuyu.barhopping.rank.route

import android.util.Log
import android.widget.CompoundButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.NewRouteStore
import com.yuyu.barhopping.data.RouteStore


class RouteRankViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _routeItem = MutableLiveData<List<RouteStore>>()
    val routeItem: LiveData<List<RouteStore>>
        get() = _routeItem

    private val list = mutableListOf<RouteStore>()

    private val _navigateToDetail = MutableLiveData<NewRouteStore>()
    val navigateToDetail: LiveData<NewRouteStore>
        get() = _navigateToDetail

    val collectionCheckBox = MutableLiveData<Boolean>()

//    lateinit var collectionList: List<String>
    private val _collectionList = MutableLiveData<List<String>>()
    val collectionList: LiveData<List<String>>
        get() = _collectionList

    val collectAndRouteMediatorLiveData = MediatorLiveData<Pair<List<RouteStore>?, List<String>?>>()

    init {
        snapRouteData()
        getUserCollection()
    }

    fun getCheckedListener(): CompoundButton.OnCheckedChangeListener {
        val listener = CompoundButton.OnCheckedChangeListener { view, isCheck ->
            val pos = view.tag as Int

            if(routeItem.value?.size ?: 0 > 0) {
                routeItem.value?.get(pos)?.id?.let {
                    if (isCheck) {
                        uploadRouteCollection(it)
                    } else {
                        deleteRouteCollection(it)
                    }
                }
            }
        }
        return listener
    }

    private fun getUserCollection() {
        UserManager.user?.id?.let {
            db.collection("User")
                .document(it)
                .collection("routeCollection")
                .get()
                .addOnSuccessListener { document ->
                    val collection = mutableListOf<String>()

                    if (document != null) {
                        for(doc in document) {
                            collection.add(doc.id)
                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                    _collectionList.value = collection
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    fun addCollectionAndRouteToMediator() {

        collectAndRouteMediatorLiveData.addSource(_routeItem) {
            collectAndRouteMediatorLiveData.value = Pair(it, collectionList.value)
        }
        collectAndRouteMediatorLiveData.addSource(_collectionList) {
            collectAndRouteMediatorLiveData.value = Pair(routeItem.value, it)
        }
    }

    fun checkUserColleciton() {
        collectionList.value?.forEach { collectId ->
            routeItem.value?.forEach { route ->
                if (collectId == route.id) {

                }
            }
        }
    }

    private fun snapRouteData() {
        db.collection("Routes").addSnapshotListener { snapshot, e ->
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

                // TODO: 篩選數量 or 評分排行 submit this
            }
        }
    }

    fun uploadRouteCollection(routeId: String) {
        UserManager.user?.id?.let {

            val id = HashMap<String, String>()
            id["id"] = routeId

            db.collection("User")
                .document(it)
                .collection("routeCollection")
                .document(routeId)
                .set(id)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                }
                .addOnFailureListener {
                    e -> Log.w(TAG, "Error writing document", e)
                }
        }
    }

    fun deleteRouteCollection(routeId: String) {
        UserManager.user?.id?.let {
            db.collection("User")
                .document(it)
                .collection("routeCollection")
                .document(routeId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        }
    }

    fun navigateToDetail(routeStore: NewRouteStore) {
        _navigateToDetail.value = routeStore
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }

    companion object {
        private val TAG = RouteRankViewModel::class.java.simpleName
    }
}