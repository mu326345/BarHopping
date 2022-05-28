package com.yuyu.barhopping.map

import android.content.ContentValues
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.SphericalUtil
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.*
import com.yuyu.barhopping.network.DirectionApi
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource
import com.yuyu.barhopping.util.getMarketType
import com.yuyu.barhopping.util.timeNow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max


class MapViewModel(val repository: FirebaseRepository) : ViewModel() {

    private val db = Firebase.firestore

    private val storage = Firebase.storage

    private val _moveCamera = MutableLiveData<LatLng>()
    val moveCamera: LiveData<LatLng>
        get() = _moveCamera

    val selectedLocationName = MutableLiveData<String>()

    private val _marketMarkers = MutableLiveData<List<MarketMarkerDataItem>>()
    val marketMarkers: LiveData<List<MarketMarkerDataItem>>
        get() = _marketMarkers

    val sevenChecked = MutableLiveData(false)
    val familyChecked = MutableLiveData(false)
    val hiLifeChecked = MutableLiveData(false)
    val okMartChecked = MutableLiveData(false)

    // newest

    private val _onRoute = MutableLiveData<Boolean>()
    val onRoute: LiveData<Boolean>
        get() = _onRoute

    // steps
    private val _currentStep = MutableLiveData<StepTypeFilter>()
    val currentStep: LiveData<StepTypeFilter>
        get() = _currentStep

    private val _step2Paths = MutableLiveData<List<LatLng>>()
    val step2Paths: LiveData<List<LatLng>>
        get() = _step2Paths

    val currentMarketMarkers = mutableMapOf<MarketName, MutableList<Marker>>()

    var readyToRoute: ReadyToRoute? = null

    // on route
    private val _partners = MutableLiveData<List<Partner>>()
    val partners: LiveData<List<Partner>>
        get() = _partners

    private val _images = MutableLiveData<List<OnRouteUserImages>>()
    val images: LiveData<List<OnRouteUserImages>>
        get() = _images

    private val _sheetItems = MutableLiveData<List<SheetItem>>()
    val sheetItems: LiveData<List<SheetItem>>
        get() = _sheetItems

    var currentRoute: RouteStore? = null
    var currentPointDatas: List<PointData>? = null
    var currentUsers: List<Partner>? = null

    val totalProgress = Transformations.map(sheetItems) {
        it.size
    }

    val currentProgress = Transformations.map(images) { images ->
        images.filter { it.userId == UserManager.user?.id }.size
    }

    val nextProgressName = Transformations.map(currentProgress) {
        if (it != null && it < currentPointDatas?.size ?: 0) {
            currentPointDatas?.get(it)?.name
        } else {
            "--"
        }
    }

    var currentLocation: LatLng? = null

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _displayCompleted = MutableLiveData<Boolean>()
    val displayCompleted: LiveData<Boolean>
        get() = _displayCompleted

    private val _partnersFinished = MutableLiveData(false)
    val partnersFinished: LiveData<Boolean>
        get() = _partnersFinished

    private val _qrCodeReady = MutableLiveData(false)
    val qrCodeReady: LiveData<Boolean?>
        get() = _qrCodeReady

    private val _navigateToProgress = MutableLiveData<Boolean>()
    val navigateToProgress: LiveData<Boolean>
        get() = _navigateToProgress

    private val _canTakePhoto = MutableLiveData<Boolean>()
    val canTakePhoto: LiveData<Boolean>
        get() = _canTakePhoto

    init {
//        checkState()
    }

    fun checkState() {
        UserManager.user?.let {
            _onRoute.value = it.onRoute != null
        }
    }

    fun setReadyToRoute() {
        readyToRoute = ReadyToRoute()
        setReadyToRouteStep(StepTypeFilter.STEP1)
    }

    fun resetReadyToRoute() {
        Log.e("yy", "resetReadyToRoute()")
        readyToRoute = null
        _currentStep.value = null
        selectedLocationName.value = null
        _marketMarkers.value = null
        _step2Paths.value = null
        currentMarketMarkers.clear()
    }


    fun setReadyToRouteStep(nextStep: StepTypeFilter) {
        when (nextStep) {
            StepTypeFilter.STEP1 -> {
                Log.d("yy", "setReadyToRouteStep STEP1")

                // 回第一步的清空
                selectedLocationName.value = null

                // 第一步 正常流程
                _currentStep.value = nextStep
            }
            StepTypeFilter.STEP2 -> {
                Log.d("yy", "setReadyToRouteStep STEP2")

                // 回第二步的清空
                readyToRoute?.points = mutableListOf()
                sevenChecked.value = false
                familyChecked.value = false
                hiLifeChecked.value = false
                okMartChecked.value = false

                // 第二步 正常流程
                if (readyToRoute?.destinationName != null) {
                    currentLocation?.let {
                        Log.d("yy", "setReadyToRouteStep STEP2 in")
                        setStartPoint("start", it)
                        _currentStep.value = nextStep
                    }
                } else {
                    _error.value = "destination is empty"
                }
            }
            StepTypeFilter.STEP3 -> {
                Log.d("yy", "setReadyToRouteStep STEP3")

                currentRoute?.let {
                    Log.v("QQ", "qrCodeBitmap1")
                }
                if (readyToRoute?.points?.size ?: 0 > 0) {
                    _currentStep.value = nextStep
                } else {
                    _error.value = "please click marker to select"
                }
            }
        }
    }

    fun setStartPoint(name: String, point: LatLng) {
        readyToRoute?.startPoint = point
        readyToRoute?.startName = name
    }

    fun setDestinationPoint(name: String, point: LatLng) {
        readyToRoute?.destinationPoint = point
        readyToRoute?.destinationName = name
        selectedLocationName.value = name
    }

    fun onLocationBtnClick() {
        _moveCamera.value = currentLocation
    }

    fun onLocationUpdated(location: Location) {
        currentLocation = LatLng(location.latitude, location.longitude)

        currentRoute?.points?.let { points ->
            sheetItems.value?.let { sheetItems ->

                val nextIndex = sheetItems.filter { it.done }.size
                if (points.size > nextIndex) {
                    val marketLatlng = getLatlng(points[nextIndex])

                    marketLatlng?.let {
                        currentLocation?.let { currentLocation ->
                            _canTakePhoto.value = canTakePhoto(currentLocation, it)
                        }
                    }
                }
            }
        }
    }

    fun onLastLocationUpdated(location: Location) {
        onLocationUpdated(location)
        updatePartnerLocation(location)
        _moveCamera.value = LatLng(location.latitude, location.longitude)
    }

    // show direction and draw route
    fun showDirection() {
        viewModelScope.launch {

            val startPoint = readyToRoute?.startPoint ?: LatLng(0.0, 0.0)
            val destinationPoint = readyToRoute?.destinationPoint ?: LatLng(0.0, 0.0)
            val origin = "${startPoint.latitude},${startPoint.longitude}"
            val destination = "${destinationPoint.latitude},${destinationPoint.longitude}"


            var waypoints = ""
            val selectMarketIds = readyToRoute?.points?.map {
                it.marketId
            }
            val tempList = selectMarketIds?.map { "place_id:$it" }

            waypoints = "optimize:true|" + tempList?.joinToString(separator = "|")

            try {
                val directionResult = DirectionApi.retrofitService.getDirectionResult(
                    origin,
                    destination,
                    waypoints
                )

                readyToRoute?.distance = countDistance(directionResult).toLong()

                if (directionResult.routes.size <= 0) {
                    Log.w("MapViewModel", "No routes")

                } else {
                    val path = mutableListOf<LatLng>()
                    var alreadyAddStart = false
                    for (i in 0 until (directionResult.routes[0].legs.size)) {
                        for (j in 0 until directionResult.routes[0].legs[i].steps.size) {
                            if (!alreadyAddStart) {
                                val startLatLng = LatLng(
                                    directionResult.routes[0].legs[i].steps[j].start_location.lat,
                                    directionResult.routes[0].legs[i].steps[j].start_location.lng
                                )
                                path.add(startLatLng)
                                alreadyAddStart = true
                            }
                            val endLatLng = LatLng(
                                directionResult.routes[0].legs[i].steps[j].end_location.lat,
                                directionResult.routes[0].legs[i].steps[j].end_location.lng
                            )
                            path.add(endLatLng)
                        }
                    }

                    // api 自動排序 value = Int
                    val apiOrderList = directionResult.routes[0].waypoint_order
                    // 還沒排序marketId
                    if (apiOrderList.size > 0) {
                        val nonOrderlist =
                            directionResult.geocoded_waypoints.filterIndexed { index, it ->
                                index >= 1 && index < directionResult.geocoded_waypoints.size - 1
                            }.map {
                                it.place_id.toString()
                            }

                        // 排序後的list
                        val orderPlaceIdList = mutableListOf<String>()
                        for (x in 0 until apiOrderList.size) {
                            apiOrderList.forEach {
                                if (x == it) {
                                    orderPlaceIdList.add(nonOrderlist[x])
                                }
                            }
                        }

                        // point place Id list
                        readyToRoute?.sortedPlaceIds?.clear()
                        readyToRoute?.sortedPlaceIds?.addAll(orderPlaceIdList)
                    }

                    _step2Paths.value = path
                    setRoutePath(path)
                    getAllMarketResult(path)
                }

            } catch (e: NullPointerException) {
                Log.e("MapViewModel", "NullPointerException")
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun countDistance(directionResult: GoogleMapDTO): Int {
        var distance = 0
        for (x in 0 until (directionResult.routes[0].legs.size)) {
            distance += directionResult.routes[0].legs[x].distance.value
        }
        return distance
    }

    fun getAllMarketResult(paths: List<LatLng>) {
        viewModelScope.launch {
            try {

                val middlePoint = paths[paths.size / 2]
                val radiusA = SphericalUtil.computeDistanceBetween(paths[0], middlePoint)
                val radiusB =
                    SphericalUtil.computeDistanceBetween(paths[paths.size - 1], middlePoint)
                val radius = max(radiusA, radiusB).toInt()


                val items = mutableListOf<MarketMarkerDataItem>()

                for (market in MarketName.values()) {
                    val result = DirectionApi.retrofitService.getNearbyMarket(
                        "${middlePoint.latitude},${middlePoint.longitude}",
                        radius,
                        market.value //"7-11, 全家" keyword
                    )
//                    currentMarketsList[market] = result.results
                    Log.w("yy", "result=${result.results}")
                    for (marketResult in result.results) {

                        val item = MarketMarkerDataItem(
                            market,
                            marketResult.place_id,
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        marketResult.geometry.location.lat,
                                        marketResult.geometry.location.lng
                                    )
                                )
                                .title(marketResult.name)
                                .alpha(0.6f)
                                .visible(false)
                        )
                        items.add(item)
                    }
                }

                Log.i("yy", "items=${items}")
                _marketMarkers.value = items
                onNavigateProgress()

            } catch (e: NullPointerException) {
                Log.e("MapViewModel", "${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e("MapViewModel", "${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun setMarkersVisibility(marketName: MarketName, isVisible: Boolean) {
        val markers = currentMarketMarkers[marketName] // marketMarkerList
        markers?.let {
            for (marker in markers) {
                marker.isVisible = isVisible
            }
        }
    }

    fun onMarkerClick(marker: Marker) {
        var isExist = false
        val marketData = PointData(
            marker.position.latitude,
            marker.position.longitude,
            marker.tag.toString(),// MarketId
            marker.title.toString(),
            marker.title?.getMarketType()
        )

        // 先判斷是否存在

        isExist = readyToRoute?.points?.find {
            it.marketId == marker.tag
        } != null


        // 存在 -> 移除，不存在 -> 加入
        if (isExist) {
            marker.alpha = 0.6f
            readyToRoute?.points?.remove(marketData)
        } else {
            marker.alpha = 1f
            readyToRoute?.points?.add(marketData)
        }
    }

    fun checkAndUploadPoints(points: List<PointData>) {
        Log.i("yy", "points=$points")
        val ids = points.map {
            it.marketId
        }

        db.collection("Points")
            .whereIn(FieldPath.documentId(), ids) // 1.找出相同pointDocId list
            .get()
            .addOnSuccessListener { documents ->
                var existPointIdList = mutableListOf<String>()

                documents.map { it.id }.forEach {
                    existPointIdList.add(it) // 2.已經存在的DocId list
                }

                val diffrentIdDatas = points.filterNot { select ->
                    existPointIdList.contains(select.marketId) //3. 找出不一樣的DocId
                }

                var count = diffrentIdDatas.size
                fun checkCount() {
                    if (count == 0) {
                        uploadNewRoute()
                    }
                }

                checkCount()

                diffrentIdDatas.forEach {
                    db.collection("Points").document(it.marketId)
                        .set(it).addOnCompleteListener {
                            count--
                            checkCount()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    /**
     * 第一次，在onRoute 新增partner 文件
     */
    fun joinToRoute(routeId: String) {
        UserManager.user?.let {
            val document = db.collection("Routes")
                .document(routeId)
                .collection("Partners")
                .document(it.id)

            val partner = Partner(
                id = it.id,
                userId = it.id,
                lat = currentLocation?.latitude.toString(),
                lng = currentLocation?.longitude.toString(),
                name = it.name,
                imageUrl = it.icon,
                finished = false
            )
            document.set(partner)
        }
    }

    /**
     * update user location
     */
    fun updatePartnerLocation(location: Location) {
        UserManager.user?.let { user ->
            user.onRoute?.let {
                db.collection("Routes")
                    .document(it)
                    .collection("Partners")
                    .document(user.id)
                    .update(
                        "lat", location.latitude.toString(),
                        "lng", location.longitude.toString()
                    )
            }
        }
    }

    fun uploadUserCurrentRouteId(routeId: String) {

        UserManager.user?.id?.let {
            db.collection("User")
                .document(it)
                .update("onRoute", routeId)
                .addOnSuccessListener {
                    Log.d(
                        ContentValues.TAG,
                        "DocumentSnapshot successfully updated!"
                    )

                    UserManager.user?.onRoute = routeId
                    _onRoute.value = true

                    onQrCodeReady()
                }
                .addOnFailureListener { e ->
                    Log.w(
                        ContentValues.TAG,
                        "Error updating document",
                        e
                    )
                }
        }
    }

    fun clearUserCurrentRouteId() {

        UserManager.user?.id?.let {
            db.collection("User")
                .document(it)
                .update("onRoute", null)
                .addOnSuccessListener {
                    Log.d(
                        ContentValues.TAG,
                        "DocumentSnapshot successfully updated!"
                    )

                    UserManager.user?.onRoute = null
                    _onRoute.value = false

                }
                .addOnFailureListener { e ->
                    Log.w(
                        ContentValues.TAG,
                        "Error updating document",
                        e
                    )
                }
        }
    }

    fun uploadImageToStorage(uri: Uri) {
        val imagesRef = storage.reference.child("images/${uri.lastPathSegment}")
        val uploadTask = imagesRef.putFile(uri)

        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // 獲取下載網址
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imagesRef.downloadUrl

            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    addImage(downloadUri)
                } else {
                    // Handle failures
                }
            }
        }
    }

    fun addImage(uri: Uri) {
        val imageRef = db.collection("Routes")
            .document(currentRoute?.id ?: "")
            .collection("Images")
            .document()

        currentRoute?.points?.let { points ->
            sheetItems.value?.let { sheetItems ->

                val count = sheetItems.filter { it.done }.size

                val image = OnRouteUserImages(
                    id = imageRef.id,
                    pointId = points[count],
                    url = uri.toString(),
                    userId = UserManager.user?.id ?: ""
                )

                imageRef.set(image)
            }
        }
    }

    companion object {
        val TAG = "MapViewModel"
    }

// TODO: 找地方使用
    fun canTakePhoto(currentLatLng: LatLng, nextMarketLatLng: LatLng): Boolean {
        val distance = SphericalUtil.computeDistanceBetween(
            currentLatLng, nextMarketLatLng
        ).toInt()

        return distance <= 20
//        return true
    }

    // 1.3.0 flow gogo

    /**
     * path routeId then get detail
     * and transformation get market point id list
     */
    fun getRouteDetail(routeId: String) {
        repository.getRouteDetail(object : FirebaseDataSource.RouteCallBack {
            override fun onResult(route: RouteStore) {
                currentRoute = route
                route.points?.let {
                    getPointDetailList(it)
                }
            }
        }, routeId)
    }

    fun getPathLatlngs(paths: List<String>): List<LatLng> {
        val pathLatlngs = mutableListOf<LatLng>()

        paths.forEach {
            val latLngToDouble = it.split(",")
            val lat = latLngToDouble[0].toDouble()
            val lng = latLngToDouble[1].toDouble()
            pathLatlngs.add(LatLng(lat, lng))
        }
        return pathLatlngs
    }

    fun getPointDetailList(pointIds: List<String>) {
        repository.getPointDetailList(object : FirebaseDataSource.PointCallBack {
            override fun onResult(pointDatas: List<PointData>) {
                currentPointDatas = pointDatas

                currentRoute?.id?.let {
                    getPartners(it)
                }
            }
        }, pointIds)
    }

    fun getPartners(routeId: String) {
        // one time
        repository.getOnRoutePartners(object : FirebaseDataSource.UserRoutePartnerCallBack {
            override fun onResult(partners: List<Partner>) {
                currentUsers = partners

                currentRoute?.id?.let {
                    fetchLiveImages(it)
                    fetchLivePartners(it)
                }
            }
        }, routeId)
    }

    fun fetchLivePartners(routeId: String) {
        repository.snapOnRoutePartner(object : FirebaseDataSource.UserRoutePartnerCallBack {
            override fun onResult(partners: List<Partner>) {
                val p = partners.filter { it.userId != UserManager.user?.id }
                currentUsers = p
                _partners.value = partners.filter { it.userId != UserManager.user?.id }

                // 全部partner都結束
                val isAllFinished = partners.filter { it.finished }.size == partners.size
                Log.d("yy", "isAllFinished = $isAllFinished")
                if (isAllFinished) {
                    _partnersFinished.value = true
                    deleteUserRoute(partners) // 清空user routeId
                    UserManager.user?.onRoute = null
                    _onRoute.value = false
                }
            }
        }, routeId)
    }

    /**
     * snap route-images all that for check user finish this route or not
     */
    fun fetchLiveImages(routeId: String) {
        repository.getUserRouteImages(object : FirebaseDataSource.UserRouteImagesCallBack {
            override fun onResult(images: List<OnRouteUserImages>) {
                _images.value = images
            }
        }, routeId)
    }

    suspend fun checkProgress(images: List<OnRouteUserImages>) {
        withContext(Dispatchers.Main) {
            val positionMyself = images.filter { it.userId == UserManager.user?.id }.size - 1

            val items = mutableListOf<SheetItem>()
            currentPointDatas?.let { points ->
                for ((index, point) in points.withIndex()) {
                    val sheetItem = SheetItem(
                        name = point.name,
                        done = positionMyself >= index,
                        count = positionMyself
                    )
                    items.add(sheetItem)
                }
            }

            currentUsers?.let { partners ->
                val partner = partners.filter { it.userId != UserManager.user?.id }
                for (p in partner) {
                    val imageCount = images.filter { it.userId == p.userId }.size
                    val position = imageCount - 1
                    if (position >= 0) {
                        items[position].partners.add(p)
                    }
                }
                _sheetItems.value = items
            }

            // check user finished?
            currentPointDatas?.let { points ->
                if (points.size - 1 == positionMyself) {
                    _displayCompleted.value = true

                    UserManager.user?.let {
                        uploadPartnerFinished(it.onRoute ?: "", it.id)
                    }

                    updateRouteIdToUser(points.size)
                }
            }
        }
    }

    fun uploadPartnerFinished(routeId: String, partnerId: String) {
        db.collection("Routes")
            .document(routeId)
            .collection("Partners")
            .document(partnerId)
            .update("finished", true)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                } else {
                    _error.value = "upload route fail ^.<"
                }
            }
    }

    fun deleteUserRoute(partners: List<Partner>) {
//         清空user onRoute Id
        partners.forEach {
            db.collection("User")
                .document(it.userId)
                .update("onRoute", null)
        }
    }

    fun getLatlng(pointId: String): LatLng? {

        val list = currentPointDatas?.filter { it.marketId == pointId }

        return if (list.isNullOrEmpty()) {
            null
        } else {
            LatLng(list.first().latitude, list.first().longitude)
        }
    }

    fun setRoutePath(paths: List<LatLng>) {
        readyToRoute?.paths?.clear()
        paths.map {
            readyToRoute?.paths?.add("${it.latitude},${it.longitude}")
        }
    }

    fun onCompletedDisplayed() {
        _displayCompleted.value = null
    }

    fun onPartnersCompleteDisplayed() {
        _partnersFinished.value = false
    }

    fun onErrorDisplayed() {
        _error.value = null
    }

    fun onCameraMoved() {
        _moveCamera.value = null
    }

    fun newRoute() {

        if (readyToRoute == null) {
            _error.value = "something wrong"
        } else {

            readyToRoute?.points?.let {
                checkAndUploadPoints(it)
            }
        }
    }

    fun onQrCodeReady() {
        UserManager.user?.onRoute?.let {
            _qrCodeReady.value = true
        }
    }

    fun resetQrCode() {
        _qrCodeReady.value = null
    }

    /**
     * 按照step flow會用到的新增new Route
     */
    fun uploadNewRoute() {

        var document = db.collection("Routes").document()

        val route = RouteStore(
            id = document.id,
            startPoint = "起點",
            startLat = "${readyToRoute?.startPoint?.latitude}",
            startLon = "${readyToRoute?.startPoint?.longitude}",
            endPoint = "${readyToRoute?.destinationName}",
            endLat = "${readyToRoute?.destinationPoint?.latitude}",
            endLon = "${readyToRoute?.destinationPoint?.longitude}",
            marketCount = readyToRoute?.points?.size?.toLong() ?: 0,
            length = readyToRoute?.distance ?: 0,
            hardDegree = null,
            comments = null,
            points = readyToRoute?.sortedPlaceIds?.toList(),
            paths = readyToRoute?.paths?.toList(),
            time = timeNow(),
            userName = UserManager.user?.name,
            userIcon = UserManager.user?.icon,
            userId = UserManager.user?.id
        )

        document.set(route).addOnCompleteListener {
            if (it.isSuccessful) {

                joinToRoute(document.id)
                // change to routing
                uploadUserCurrentRouteId(document.id)
            } else {
                _error.value = "upload route fail ^.<"
            }
        }
    }

    /**
     * 從rank一鍵開啟新遊戲的複製的 new route
     */
    fun uploadOldRoute(baseRoute: NewRouteStore) {

        var document = db.collection("Routes").document()

        val route = RouteStore(
            id = document.id,
            startPoint = baseRoute.startPoint,
            startLat = baseRoute.startLat,
            startLon = baseRoute.startLon,
            endPoint = baseRoute.endPoint,
            endLat = baseRoute.endLat,
            endLon = baseRoute.endLon,
            marketCount = baseRoute.marketCount,
            length = baseRoute.length,
            hardDegree = null,
            comments = null,
            points = baseRoute.points,
            paths = baseRoute.paths,
            time = timeNow(),
            userName = UserManager.user?.name,
            userIcon = UserManager.user?.icon,
            userId = UserManager.user?.id
        )

        document.set(route).addOnCompleteListener {
            if (it.isSuccessful) {

                joinToRoute(document.id)
                // change to routing
                uploadUserCurrentRouteId(document.id)
            } else {
                _error.value = "upload route fail ^.<"
            }
        }
    }

    fun navigateToProgress() {
        _navigateToProgress.value = true
    }

    fun onNavigateProgress() {
        _navigateToProgress.value = false
    }

    fun onCanTakePhoto() {
        _canTakePhoto.value = false
    }

    fun updateRouteIdToUser(marketCount: Int) {
        UserManager.user?.let {
            db.collection("User")
                .document(it.id)
                .update("marketCount", FieldValue.arrayUnion(marketCount))
            .addOnSuccessListener { Log.d("PostViewModel", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("PostViewModel", "Error writing document", e) }
        }

    }
}