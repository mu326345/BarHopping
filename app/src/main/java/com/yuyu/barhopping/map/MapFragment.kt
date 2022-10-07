package com.yuyu.barhopping.map

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Polyline
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.yuyu.barhopping.*
import com.yuyu.barhopping.R
import com.yuyu.barhopping.data.*
import com.yuyu.barhopping.databinding.FragmentMapBinding
import com.yuyu.barhopping.factory.ViewModelFactory
import com.yuyu.barhopping.map.sheet.BottomSheetAdapter
import com.yuyu.barhopping.util.PermissionUtils
import com.yuyu.barhopping.util.PermissionUtils.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapFragment : Fragment(),
    OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: FragmentMapBinding
    private val viewModel by viewModels<MapViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }

    private lateinit var adapter: MapAdapter
    private lateinit var sheetAdapter: BottomSheetAdapter

    private var map: GoogleMap? = null
    private var permissionDenied = false
    private var polyline: Polyline? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var currentMapMarker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupPlaceApi()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val stepRecyclerView = binding.stepRecycler
        stepRecyclerView.layoutManager = object : LinearLayoutManager(context, HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean = false
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.detailSheet.root.visibility = View.GONE
        binding.detailSheet.viewModel = viewModel
        sheetAdapter = BottomSheetAdapter()

        val detailSheet = binding.detailSheet
        val sheetRecycler = detailSheet.routeDetailRecycler

        initBottomSheet()

        sheetRecycler.adapter = sheetAdapter
        sheetRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        setFragmentResultListener("routeIdKey") { requestKey, bundle ->
            val result = bundle.getParcelable<NewRouteStore>("routeId")

            if(result != null) {
                viewModel.uploadOldRoute(result)
            }
        }

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.stepRecycler)

        adapter = MapAdapter(
            viewModel,
            object : View.OnClickListener {
                override fun onClick(view: View?) {
                    when (view?.id) {
                        // step1: 0, step2: 1, Step3: 2
                        R.id.next_step_btn -> { // step 1 to step 2
                            viewModel.setReadyToRouteStep(StepTypeFilter.STEP2) }

                        R.id.next_step_btn2 -> viewModel.setReadyToRouteStep(StepTypeFilter.STEP3)

                        R.id.previous_step_btn2 -> viewModel.setReadyToRouteStep(StepTypeFilter.STEP1)

                        R.id.previous_step_btn3 -> viewModel.setReadyToRouteStep(StepTypeFilter.STEP2)

                        R.id.start_game_btn -> viewModel.newRoute()
                    }
                }
            },
            object : View.OnFocusChangeListener {
                override fun onFocusChange(view: View?, focus: Boolean) {
                    if (focus) {
                        context?.let {
                            val intent = Autocomplete.IntentBuilder(
                                AutocompleteActivityMode.OVERLAY,
                                fields
                            ).setCountry("TW")
                                .build(it)

                            when (view?.id) {
                                R.id.destination_edit -> {
                                    startActivityForResult(intent, DESTINATION_REQUEST_CODE)
                                }
                            }
                            view?.clearFocus()
                        }
                    }
                }
            }
        )

        binding.stepRecycler.adapter = adapter
        adapter.submitList(listOf(StepTypeFilter.STEP1, StepTypeFilter.STEP2, StepTypeFilter.STEP3))

        binding.gameOverBtn.setOnClickListener {
            map?.clear()
            viewModel.clearUserCurrentRouteId()
            viewModel.setReadyToRouteStep(StepTypeFilter.STEP1)
            dismissCameraAndHint()
        }

        // camera and QR code
        binding.cameraBtn.setOnClickListener {
            startCamera()
            binding.takePhotoCard.visibility = View.INVISIBLE
        }

        binding.qrScannerBtn.setOnClickListener {
            findNavController().navigate(MapFragmentDirections.navigateToQrCodeScannerFragment())

            setFragmentResultListener("qrCodeResult") { requestKey, bundle ->
                val result = bundle.getString("qrCodeKey")

                if(result != null) {
                    resetMap()
                    viewModel.joinToRoute(result)
                    viewModel.uploadUserCurrentRouteId(result)
                }
            }
        }

        binding.qrCodeBtn.setOnClickListener {
            findNavController().navigate(MapFragmentDirections.navigateToQrCodeDialogFragment())
        }

        binding.locationBtn.setOnClickListener {
            viewModel.onLocationBtnClick() // remove camera to location
        }


        viewModel.moveCamera.observe(viewLifecycleOwner) {
            it?.let {
                moveCameraToMarker(it)
                viewModel.onCameraMoved()
            }
        }

        viewModel.step2Paths.observe(viewLifecycleOwner) {
            polyline?.remove()
            it?.let {
                val lineOption = PolylineOptions()

                lineOption.addAll(it)
                lineOption.width(10f)
                lineOption.color(Color.BLUE)
                lineOption.geodesic(true)
                polyline = map?.addPolyline(lineOption)!!
            }
        }

        // get nearby result set market Icon
        viewModel.marketMarkers.observe(viewLifecycleOwner) {
            Log.d("yy", "viewModel.marketMarketMarkers.observe, it=$it")

            it?.let {
                it.forEach { item ->
                    val marker = map?.addMarker(item.marketOptions)?.apply {
                        tag = item.placeId

                        when (item.markerName) {
                            MarketName.SEVEN -> {
                                setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sevenmarker2))
                            }

                            MarketName.FAMILY -> {
                                setIcon(BitmapDescriptorFactory.fromResource(R.drawable.familymarker2))
                            }

                            MarketName.HILIFE -> {
                                setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hilifemarker2))
                            }

                            MarketName.OKMART -> {
                                setIcon(BitmapDescriptorFactory.fromResource(R.drawable.okmarker2))
                            }
                        }
                    }

                    marker?.let { // 判斷沒有marker -> 儲存一個markerList
                        if (viewModel.currentMarketMarkers[item.markerName] == null) {
                            viewModel.currentMarketMarkers[item.markerName] = mutableListOf()
                        }
                        viewModel.currentMarketMarkers[item.markerName]?.add(marker)
                    }
                }
                viewModel.setMarkersVisibility(MarketName.SEVEN, isVisible)
            }
        }

        // markets check box
        viewModel.sevenChecked.observe(viewLifecycleOwner) {
            it?.let { isVisible ->
                viewModel.setMarkersVisibility(MarketName.SEVEN, isVisible)
            }
        }

        viewModel.familyChecked.observe(viewLifecycleOwner) {
            it?.let { isVisible ->
                viewModel.setMarkersVisibility(MarketName.FAMILY, isVisible)
            }
        }

        viewModel.hiLifeChecked.observe(viewLifecycleOwner) {
            it?.let { isVisible ->
                viewModel.setMarkersVisibility(MarketName.HILIFE, isVisible)
            }
        }

        viewModel.okMartChecked.observe(viewLifecycleOwner) {
            it?.let { isVisible ->
                viewModel.setMarkersVisibility(MarketName.OKMART, isVisible)
            }
        }


        viewModel.onRoute.observe(viewLifecycleOwner) { isRouting ->
            isRouting.let {
                initRouteUi(isRouting)

                viewModel.resetReadyToRoute()
                if (isRouting) {
                    Log.d("yy", "on route")
                    setupCurrentRoute(UserManager.user?.onRoute ?: "")

                } else {
                    Log.d("yy", "not on route")
                    viewModel.setReadyToRoute()
                }
            }
        }

        viewModel.currentStep.observe(viewLifecycleOwner) {
            Log.d("yy", "currentStep = $it")
            it?.let {
                when (it) {

                    StepTypeFilter.STEP1 -> {
                        Log.i("yy", "current step: 1")

                        binding.stepRecycler.scrollToPosition(0)
                        resetMap()

                        map?.setOnPoiClickListener(object : GoogleMap.OnPoiClickListener {
                            override fun onPoiClick(point: PointOfInterest) {

                                setCurrentMapMarker(point.latLng)
                                viewModel.setDestinationPoint(point.name, point.latLng)
                            }
                        })
                        viewModel.onCanTakePhoto()
                    }

                    StepTypeFilter.STEP2 -> {
                        Log.i("yy", "current step: 2")

                        binding.stepRecycler.scrollToPosition(1)
                        resetMap()

                        viewModel.navigateToProgress()
                        viewModel.showDirection()
                        viewModel.readyToRoute?.let {
                            it.destinationPoint?.let { des ->
                                it.startPoint?.let { start ->

                                    setCurrentMapMarker(des)

                                    val update = CameraUpdateFactory.newLatLngBounds(
                                        LatLngBounds.builder()
                                            .include(LatLng(start.latitude, start.longitude))
                                            .include(LatLng(des.latitude, des.longitude))
                                            .build(), 200
                                    )
                                    map?.animateCamera(update)
                                }
                            }
                        }
                    }

                    StepTypeFilter.STEP3 -> {
                        Log.i("yy", "current step: 3")

                        binding.stepRecycler.scrollToPosition(2)
                        resetMap()

                        viewModel.showDirection()
                        viewModel.readyToRoute?.points?.let {
                            addPointMarkers(it)
                        }

                        viewModel.readyToRoute?.destinationPoint?.let {
                            setCurrentMapMarker(it)
                        }
                    }
                }
            }
        }

        viewModel.navigateToProgress.observe(viewLifecycleOwner) { isProgressBar ->
            if(isProgressBar) {
                findNavController().navigate(MapFragmentDirections.navigateToProgressBarDialogFragment())

            } else {
                findNavController().popBackStack()
            }
        }

        viewModel.qrCodeReady.observe(viewLifecycleOwner) { isQrCodeReady ->
            isQrCodeReady?.let {

                if(isQrCodeReady) {
                    findNavController().navigate(MapFragmentDirections.navigateToQrCodeDialogFragment())
                }
                viewModel.resetQrCode()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let {
                val snackbar = Snackbar.make(binding.snackbarLayout, it, Snackbar.LENGTH_SHORT)
                val view = snackbar.view
                val params = view.layoutParams as CoordinatorLayout.LayoutParams
                params.gravity = Gravity.CENTER
                view.layoutParams = params
                snackbar.show()

                viewModel.onErrorDisplayed()
            }
        }

        viewModel.canTakePhoto.observe(viewLifecycleOwner) {
            if (it) {
                showCameraAndHint()
            } else {
                dismissCameraAndHint()
            }
        }

        return binding.root
    }

    /**
     * 路線設定
     */
    private fun initRouteUi(onRoute: Boolean) {
        map?.clear()
        binding.stepRecycler.scrollToPosition(0)

        if (onRoute) {
            binding.cameraCard.visibility = View.INVISIBLE
            binding.gameOverBtn.visibility = View.VISIBLE
            binding.detailSheet.root.visibility = View.VISIBLE
            animateY(binding.detailSheet.root)
            binding.stepRecycler.visibility = View.GONE
            binding.qrCodeCard.visibility = View.VISIBLE
            (activity as MainActivity).hideBottomNav()

        } else {
            binding.cameraCard.visibility = View.GONE
            binding.gameOverBtn.visibility = View.GONE
            binding.detailSheet.root.visibility = View.GONE
            binding.stepRecycler.visibility = View.VISIBLE
            binding.qrCodeCard.visibility = View.GONE
            (activity as MainActivity).showBottomNav()
        }
    }

    private fun setupCurrentRoute(routeId: String) {

        viewModel.getRouteDetail(routeId)

        viewModel.images.observe(viewLifecycleOwner) {
            Log.d("Wayne", "viewModel.images.observe, it=$it")
            it?.let { images ->
                addMarkers(
                    viewModel.currentRoute?.paths ?: emptyList(),
                    viewModel.currentPointDatas ?: emptyList(),
                    images
                )
            }
        }

        viewModel.sheetItems.observe(viewLifecycleOwner) {
            it?.let {
                sheetAdapter.submitList(it)
                sheetAdapter.notifyDataSetChanged()
            }
        }

        viewModel.partners.observe(viewLifecycleOwner) {
            it?.let {
                addPartnerMarkers(it)
            }
        }

        // check finished
        viewModel.displayCompleted.observe(viewLifecycleOwner) {
            it?.let {
//                Toast.makeText(context, "user finished", Toast.LENGTH_SHORT).show()
                viewModel.onCompletedDisplayed()
            }
        }

        viewModel.partnersFinished.observe(viewLifecycleOwner) { allFinished ->
            if (allFinished) {
//                Toast.makeText(context, "all user finished", Toast.LENGTH_SHORT).show()
                findNavController().navigate(MapFragmentDirections.navigateToAllSuccessDialogFragment())
                viewModel.onPartnersCompleteDisplayed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    /**
     * 地圖設定
     */
    private fun resetMap() {
        map?.setOnPoiClickListener(null)
        map?.clear()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.setOnMarkerClickListener(setOnMarkerClickListener)
        googleMap.setOnMyLocationClickListener(onMyLocationClickListener)
        googleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener)

        getLastLocation()
        enableMyLocation()
        startLocationUpdates()

        viewModel.checkState()
        initMapFirstLocation()
    }

    private fun initMapFirstLocation() {
        val taiwan = LatLng(23.929964280259153, 121.09061838278878)

        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                taiwan, FIRST_ZOOM.toFloat()
            )
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions, grantResults, Manifest.permission.`ACCESS_COARSE_LOCATION`
            )
        ) {
            enableMyLocation()
            startLocationUpdates()

        } else {
            permissionDenied = true
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            map?.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(childFragmentManager, "dialog")
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * autoComplete and imagePicker result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {

            DESTINATION_REQUEST_CODE -> {
                when (resultCode) {

                    Activity.RESULT_OK -> {
                        data?.let {
                            val place = Autocomplete.getPlaceFromIntent(data)

                            setCurrentMapMarker(place.latLng ?: LatLng(0.0, 0.0))
                            moveCameraToMarker(place.latLng ?: LatLng(0.0, 0.0))

                            viewModel.setDestinationPoint(
                                place.name ?: "",
                                place.latLng ?: LatLng(0.0, 0.0))
                            viewModel.onCameraMoved()
                        }
                    }

                    AutocompleteActivity.RESULT_ERROR -> {
                        data?.let {
                            val status = Autocomplete.getStatusFromIntent(data)

                            Log.i(TAG, status.statusMessage ?: "")
                        }
                    }

                    Activity.RESULT_CANCELED -> {
                        // The user canceled the operation.
                    }
                }
                return
            }

            // imagePicker
            CAMERA_IMAGE_REQ_CODE -> {
                when (resultCode) {

                    Activity.RESULT_OK -> {
                        data?.data?.let {
                            viewModel.onCanTakePhoto()
                            viewModel.uploadImageToStorage(it)
                        }
                    }

                    ImagePicker.RESULT_ERROR -> {
//                        Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    }

                    else -> {
//                        Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    viewModel.onLastLocationUpdated(location)
                }
            }
    }

    // update location
    fun createLocationRequest() = LocationRequest.create()?.apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            createLocationRequest(),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun setupPlaceApi() {
        // Initialize the SDK
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)

        // Create a new PlacesClient instance
        placesClient = Places.createClient(requireContext())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * map ui: marker, paths, marketMarker, imageMarker
     */
    private fun addMarkers(paths: List<String>, points: List<PointData>, images: List<OnRouteUserImages>) {

        addPathMarkers(paths)
        addPointMarkers(points)
        addImageMarkers(images)
    }

    private fun addPathMarkers(paths: List<String>) {
        val pathLatlngs = viewModel.getPathLatlngs(paths)

        polyline?.remove()
        val lineOption = PolylineOptions()
        lineOption.addAll(pathLatlngs)
        lineOption.width(10f)
        lineOption.color(resources.getColor(R.color.huang_liu))
        lineOption.geodesic(true)
        polyline = map?.addPolyline(lineOption)!!
    }

    private fun addPointMarkers(points: List<PointData>) {
        points.forEach { point ->

            when (point.type) {

                1 -> {
                    map?.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.sevenmarker2))
                            .zIndex(DEFAULT_Z_INDEX)
                    )
                }

                2 -> {
                    map?.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.familymarker2))
                            .zIndex(DEFAULT_Z_INDEX)
                    )
                }

                3 -> {
                    map?.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.hilifemarker2))
                            .zIndex(DEFAULT_Z_INDEX)
                    )
                }

                4 -> {
                    map?.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.okmarker2))
                            .zIndex(DEFAULT_Z_INDEX)
                    )
                }

                else -> null
            }
        }

        addOriAndDesMarkers()
    }

    private fun addImageMarkers(images: List<OnRouteUserImages>) {
        val cacheMaps = mutableMapOf<LatLng, Int>()

        lifecycleScope.launch(Dispatchers.IO) {
            for (image in images) {
                viewModel.getLatlng(image.pointId)?.let { latlng ->

                    if (cacheMaps.containsKey(latlng)) {
                        cacheMaps[latlng] = cacheMaps[latlng]?.plus(1) ?: 1
                    } else {
                        cacheMaps[latlng] = 1
                    }

                    addMarkerAndLoadImage(
                        image.url,
                        LatLng(latlng.latitude + ((cacheMaps[latlng] ?: 1) * DISPLACEMENT),
                        latlng.longitude + ((cacheMaps[latlng] ?: 1) * DISPLACEMENT)))
                }
            }

            viewModel.checkProgress(images)
        }
    }

    private suspend fun addMarkerAndLoadImage(url: String, latlng: LatLng) {
            val futureTarget = Glide.with(requireContext())
                .asBitmap()
                .load(url)
                .submit(125, 125)
            val bitmap = futureTarget.get()

            withContext(Dispatchers.Main) {
                map?.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .position(latlng)
                )
            }
            Glide.with(requireContext()).clear(futureTarget)
    }

    private fun addOriAndDesMarkers() {
        // ori marker
        val startPointLatlng = LatLng(
            viewModel.currentRoute?.startLat?.toDouble() ?: 0.0,
            viewModel.currentRoute?.startLon?.toDouble() ?: 0.0
        )
        val startPointTitle = viewModel.currentRoute?.startPoint

        map?.addMarker(
            MarkerOptions()
                .position(startPointLatlng)
                .title(startPointTitle)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_circle))
                .zIndex(DEFAULT_Z_INDEX)
        )

        // des marker
        val endPointLatlng = LatLng(
            viewModel.currentRoute?.endLat?.toDouble() ?: 0.0,
            viewModel.currentRoute?.endLon?.toDouble() ?: 0.0
        )
        val endPointTitle = viewModel.currentRoute?.endPoint

        map?.addMarker(
            MarkerOptions()
                .position(endPointLatlng)
                .title(endPointTitle)
        )
    }

    // 朋友在遊戲中的頭貼
    private fun addPartnerMarkers(partners: List<Partner>) {
        partners.forEach {
            lifecycleScope.launch(Dispatchers.IO) {
                val futureTarget = Glide.with(requireContext())
                    .asBitmap()
                    .load(it.imageUrl)
                    .circleCrop()
                    .submit(100, 100)
                val bitmap = futureTarget.get()

                withContext(Dispatchers.Main) {
                    map?.addMarker(
                        MarkerOptions()
                            .position(LatLng(it.lat.toDouble(), it.lng.toDouble()))
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    )
                }
                Glide.with(requireContext()).clear(futureTarget)
            }
        }
    }

    fun setCurrentMapMarker(latLng: LatLng) {
        currentMapMarker?.remove()
        currentMapMarker = map?.addMarker(
            MarkerOptions()
                .position(latLng)
        )
    }

    private fun moveCameraToMarker(latLng: LatLng) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng, DEFAULT_ZOOM.toFloat()
            )
        )
    }

    // ImagePicker
    private fun startCamera() {
        ImagePicker.with(this)
            .cameraOnly()    //User can only capture image using Camera
            .compress(1024)
            .maxResultSize(620, 620)
            .cropSquare()
            .start(CAMERA_IMAGE_REQ_CODE)
    }

    // detail bottom sheet bottle animate
    private fun initBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.detailSheet.root)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Called every time when the bottom sheet changes its state.
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (isAdded) {
                    animateBottomSheetArrows(slideOffset)
                    transitionBottomSheetBackgroundColor(slideOffset)
                }
            }
        })
    }

    private fun animateBottomSheetArrows(slideOffset: Float) {
        binding.detailSheet.bottleRImg.rotation = slideOffset * 180
        binding.detailSheet.bottleLImg.rotation = slideOffset * -180
    }

    private fun animateY(view: View) {
        view.apply {
            ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0f, -50f, 0f).apply {
                duration = 500
                repeatCount = 2
                startDelay = 500
                start()
            }
        }
    }

    private fun animateX(view: View) {
        view.apply {
            ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0f, -50f, 0f).apply {
                duration = 500
                repeatCount = 2
                startDelay = 500
                start()
            }
        }
    }
    
    private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
        val colorFrom = resources.getColor(R.color.white)
        val colorTo = resources.getColor(R.color.animate1)
        binding.detailSheet.animateLayout.setBackgroundColor(interpolateColor(slideOffset, colorFrom, colorTo))
    }

    private fun interpolateColor(fraction: Float, startValue: Int, endValue: Int): Int {
        val startA = startValue shr 24 and 0xff
        val startR = startValue shr 16 and 0xff
        val startG = startValue shr 8 and 0xff
        val startB = startValue and 0xff
        val endA = endValue shr 24 and 0xff
        val endR = endValue shr 16 and 0xff
        val endG = endValue shr 8 and 0xff
        val endB = endValue and 0xff
        return startA + (fraction * (endA - startA)).toInt() shl 24 or
                (startR + (fraction * (endR - startR)).toInt() shl 16) or
                (startG + (fraction * (endG - startG)).toInt() shl 8) or
                startB + (fraction * (endB - startB)).toInt()
    }
    private fun showCameraAndHint() {
        binding.cameraCard.visibility = View.VISIBLE
        binding.takePhotoCard.visibility = View.VISIBLE
        animateX(binding.takePhotoCard)
    }

    private fun dismissCameraAndHint() {
        binding.cameraCard.visibility = View.GONE
        binding.takePhotoCard.visibility = View.GONE
    }

    private val onMyLocationClickListener = object : GoogleMap.OnMyLocationClickListener {
        override fun onMyLocationClick(location: Location) {
//            Toast.makeText(context, "Current location:\n$location", Toast.LENGTH_LONG).show()
        }
    }

    private val onMyLocationButtonClickListener =
        object : GoogleMap.OnMyLocationButtonClickListener {
            override fun onMyLocationButtonClick(): Boolean {
//                Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
                return false
            }
        }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return

            if (locationResult.locations.size > 0) {
                viewModel.onLocationUpdated(locationResult.locations[0])
            }
        }
    }

    private val setOnMarkerClickListener = object : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(marker: Marker): Boolean {
            viewModel.onMarkerClick(marker)

            return false
        }
    }


    companion object {
        /**
         * Request code for location permission request.
         */
        private val TAG = MapFragment::class.java.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_IMAGE_REQ_CODE = 103
        private const val DESTINATION_REQUEST_CODE = 1

        private const val DEFAULT_ZOOM = 15
        private const val FIRST_ZOOM = 7
        private const val DISPLACEMENT = 0.00009
        private const val DEFAULT_Z_INDEX = 1.0f
        val fields = listOf(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG)
    }
}