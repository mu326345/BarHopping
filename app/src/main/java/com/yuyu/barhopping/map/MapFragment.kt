package com.yuyu.barhopping.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.SphericalUtil
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.BuildConfig
import com.yuyu.barhopping.MainActivity
import com.yuyu.barhopping.R
import com.yuyu.barhopping.data.FriendsProgress
import com.yuyu.barhopping.data.MarketName
import com.yuyu.barhopping.data.SheetItem
import com.yuyu.barhopping.databinding.BottomSheetRouteDetailBinding
import com.yuyu.barhopping.databinding.FragmentMapBinding
import com.yuyu.barhopping.factory.ViewModelFactory
import com.yuyu.barhopping.map.sheet.BottomSheetAdapter
import com.yuyu.barhopping.map.sheet.BottomSheetViewModel
import com.yuyu.barhopping.util.PermissionUtils
import com.yuyu.barhopping.util.PermissionUtils.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class MapFragment : Fragment(),
    OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: FragmentMapBinding
    private var isGameStart = false
    private val viewModel: MapViewModel by viewModels()
    private val sheetViewModel by viewModels<BottomSheetViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }

    private lateinit var adapter: MapAdapter
    private lateinit var sheetAdapter: BottomSheetAdapter

    private var permissionDenied = false
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var markerMap: EnumMap<MarketName, List<Marker?>> = EnumMap(MarketName::class.java)
    private var polyline: Polyline? = null
    private var nextPointId: String? = null
    private var userOnRouteId: String? = null


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

        binding.detailSheet.lifecycleOwner = this
        binding.detailSheet.viewModel = sheetViewModel

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.detailSheet.root.visibility = View.GONE

        sheetAdapter = BottomSheetAdapter()
        val detailSheet = binding.detailSheet
        val sheetRecycler = detailSheet.routeDetailRecycler
        sheetRecycler.adapter = sheetAdapter
        sheetRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


        adapter = MapAdapter(
            viewModel,
            object : View.OnClickListener {
                override fun onClick(view: View?) {
                    when (view?.id) {
                        // step1: 0, step2: 1, Step3: 2
                        R.id.next_step_btn -> viewModel.setStepPage(StepTypeFilter.STEP2)
                        R.id.next_step_btn2 -> viewModel.setStepPage(StepTypeFilter.STEP3)
                        R.id.previous_step_btn2 -> viewModel.setStepPage(StepTypeFilter.STEP1)
                        R.id.previous_step_btn3 -> viewModel.setStepPage(StepTypeFilter.STEP2)
                        R.id.start_game_btn -> {
                            viewModel.setStepPage(StepTypeFilter.STEP1)
                            viewModel.onRouting()
                            viewModel.storeOnRoute() // and upload onRouteId to User
                            Toast.makeText(context, "Start Game!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            object : View.OnFocusChangeListener {
                override fun onFocusChange(view: View?, focus: Boolean) {
                    if (focus) {
                        context?.let {
                            when (view?.id) {
                                R.id.destination_edit -> {
                                    val intent = Autocomplete.IntentBuilder(
                                        AutocompleteActivityMode.FULLSCREEN,
                                        fields
                                    )
                                        .build(it)
                                    startActivityForResult(intent, DESTINATION_REQUEST_CODE)
                                    view.clearFocus()
                                }
                                R.id.step2_destination_edit -> {
                                    val intent = Autocomplete.IntentBuilder(
                                        AutocompleteActivityMode.FULLSCREEN,
                                        fields
                                    )
                                        .build(it)
                                    startActivityForResult(intent, STEP2_DESTINATION_REQUEST_CODE)
                                    view.clearFocus()
                                }
                                R.id.location_edit -> {
                                    val intent = Autocomplete.IntentBuilder(
                                        AutocompleteActivityMode.FULLSCREEN,
                                        fields
                                    )
                                        .build(it)
                                    startActivityForResult(intent, STEP2_LOCATION_REQUEST_CODE)
                                    view.clearFocus()
                                }
                            }
                        }
                    }
                }
            }
        )
        binding.stepRecycler.adapter = adapter
        adapter.submitList(listOf(StepTypeFilter.STEP1, StepTypeFilter.STEP2, StepTypeFilter.STEP3))

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.stepRecycler)

        // refresh location
        binding.locationBtn.setOnClickListener {
            viewModel.onLocationBtnClick()
        }

        binding.gameOverBtn.setOnClickListener {
            viewModel.notOnRouting()
        }


        viewModel.moveCameraLiveData.observe(
            viewLifecycleOwner, Observer {
                moveCameraToMarker(it)
            }
        )

        // destination marker
        viewModel.desMarkerLiveData.observe(
            viewLifecycleOwner, Observer {
                map?.clear()
                addMarkersToMap(it, viewModel.desMarkerNameLiveData.value)
            }
        )

        viewModel.paths.observe(
            viewLifecycleOwner, Observer {
                polyline?.remove()
                val lineOption = PolylineOptions()
                lineOption.addAll(it)
                lineOption.width(10f)
                lineOption.color(Color.BLUE)
                lineOption.geodesic(true)
                polyline = map?.addPolyline(lineOption)!!
            }
        )

        // nearby market marker
        viewModel.addMarkerLiveData.observe(
            viewLifecycleOwner, Observer {
                val markerList = it.marketOptions.map { map?.addMarker(it) }
                markerMap.put(it.markerName, markerList)

                markerList.forEachIndexed {index,  marker ->
                    marker?.tag = it.placeIds[index]
                }
            }
        )

        viewModel.visibleMarker.observe(
            viewLifecycleOwner, Observer {
                val marketValueList = markerMap.get(it.first)
                marketValueList?.forEach { marker ->
                    marker?.isVisible = it.second
                }
            }
        )

        // market check box
        viewModel.sevenChecked.observe(
            viewLifecycleOwner, Observer {
                lifecycleScope.launch(Dispatchers.IO) {
                    val futureTarget = Glide.with(requireContext())
                        .asBitmap()
                        .load(R.drawable.sevenmarker)
                        .submit(150, 150)
                    val bitmap = futureTarget.get()

                    withContext(Dispatchers.Main) {
                        viewModel.setMarketCheck(
                            MarketName.SEVEN,
                            it,
                            markerMap[MarketName.SEVEN] != null,
                            bitmap
                        )
                    }
                }
            }
        )

        viewModel.familyChecked.observe(
            viewLifecycleOwner, Observer {
                lifecycleScope.launch(Dispatchers.IO) {
                    val futureTarget = Glide.with(requireContext())
                        .asBitmap()
                        .load(R.drawable.family1marker)
                        .submit(150, 150)
                    val bitmap = futureTarget.get()

                    withContext(Dispatchers.Main) {
                        viewModel.setMarketCheck(
                            MarketName.FAMILY,
                            it,
                            markerMap[MarketName.FAMILY] != null,
                            bitmap
                        )
                    }
                }
            }
        )

        viewModel.hiLifeChecked.observe(
            viewLifecycleOwner, Observer {
                lifecycleScope.launch(Dispatchers.IO) {
                    val futureTarget = Glide.with(requireContext())
                        .asBitmap()
                        .load(R.drawable.hilifemarker)
                        .submit(150, 150)
                    val bitmap = futureTarget.get()

                    withContext(Dispatchers.Main) {
                        viewModel.setMarketCheck(
                            MarketName.HILIFE,
                            it,
                            markerMap[MarketName.HILIFE] != null,
                            bitmap
                        )
                    }
                }
            }
        )

        viewModel.okMartChecked.observe(
            viewLifecycleOwner, Observer {
                lifecycleScope.launch(Dispatchers.IO) {
                    val futureTarget = Glide.with(requireContext())
                        .asBitmap()
                        .load(R.drawable.okmarker)
                        .submit(150, 150)
                    val bitmap = futureTarget.get()

                    withContext(Dispatchers.Main) {
                        viewModel.setMarketCheck(
                            MarketName.OKMART,
                            it,
                            markerMap[MarketName.OKMART] != null,
                            bitmap
                        )
                    }
                }
            }
        )

        viewModel.stepPage.observe(
            viewLifecycleOwner, Observer {
                binding.stepRecycler.layoutManager?.scrollToPosition(it)
            }
        )

        /**
         * control game state and their behavior and UI
         */
        viewModel.onRoute.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    true -> {
                        isGameStart = true
                        binding.detailSheet.root.visibility = View.VISIBLE
                        stepRecyclerView.visibility = View.GONE
                        (activity as MainActivity).hideBottomNav()
                    }
                    false -> {
                        isGameStart = false
                        map?.clear()
                        stepRecyclerView.visibility = View.VISIBLE
                        binding.detailSheet.root.visibility = View.GONE
                        (activity as MainActivity).showBottomNav()
                    }
                }
            }
        )

        // make sure user have onRoute or not
        viewModel.userOnRouteId.observe(
            viewLifecycleOwner, Observer {
                it?.let {
                    userOnRouteId = it
                    viewModel.onRouting()
                    viewModel.addUserLocationToOnRoute()
                    sheetViewModel.getRouteDetail(it)
                    sheetViewModel.snapOnRouteUserImages(it)
                    sheetViewModel.snapOnRouteUserLocation(it)
                }
            }
        )

        viewModel.lastLocationLiveData.observe(
            viewLifecycleOwner, Observer {
                viewModel.updateUserLocationToOnRoute()

                it?.let {
                    if (isGameStart) {
                        val nextPoint = sheetViewModel.getNextPoint()
                        val nextLatLng = nextPoint?.first
                        nextPointId = nextPoint?.second
                        val locationLatLng = LatLng(it.latitude, it.longitude)
                        var distance = 0

                            if (nextLatLng != null) {
                                distance = SphericalUtil.computeDistanceBetween(
                                    locationLatLng, nextLatLng
                                ).toInt()
                            }

                            if (distance <= 20) {
                                binding.cameraBtn.setOnClickListener {
                                    startCamera()
                                }

//                                AlertDialog.Builder(requireContext())
//                                    .setTitle(getString(R.string.already_market))
//                                    .setMessage(getString(R.string.take_photo_content))
//                                    .setCancelable(false)
//                                    .setNeutralButton(getString(R.string.give_up)) { dialog, which ->
//                                        // TODO: game over page
//                                    }
//                                    .setPositiveButton(getString(R.string.take_photo)) { dialog, which ->
//                                        cameraIntent()
//                                        dialog.dismiss()
//                                    }.show()
                            }

                    }
                }
            }
        )

        viewModel.userSelectMarketName.observe(
            viewLifecycleOwner, Observer {
                sheetViewModel.nameAndStateInDataClass(it)
            })

        sheetViewModel.marketDetailData.observe(viewLifecycleOwner) {
            it.forEach {
                when(it.type) {
                    1 -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val futureTarget = Glide.with(requireContext())
                                .asBitmap()
                                .load(R.drawable.sevenmarker)
                                .submit(150, 150)
                            val bitmap = futureTarget.get()

                            withContext(Dispatchers.Main) {
                                map?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(it.latitude, it.longitude))
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                        .zIndex(1.0f)
                                )
                            }
                        }
                    }

                    2 -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val futureTarget = Glide.with(requireContext())
                                .asBitmap()
                                .load(R.drawable.family1marker)
                                .submit(150, 150)
                            val bitmap = futureTarget.get()

                            withContext(Dispatchers.Main) {
                                map?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(it.latitude, it.longitude))
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                        .zIndex(1.0f)
                                )
                            }
                        }
                    }

                    3 -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val futureTarget = Glide.with(requireContext())
                                .asBitmap()
                                .load(R.drawable.hilifemarker)
                                .submit(150, 150)
                            val bitmap = futureTarget.get()

                            withContext(Dispatchers.Main) {
                                map?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(it.latitude, it.longitude))
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                        .zIndex(1.0f)
                                )
                            }
                        }
                    }

                    4 -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val futureTarget = Glide.with(requireContext())
                                .asBitmap()
                                .load(R.drawable.okmarker)
                                .submit(150, 150)
                            val bitmap = futureTarget.get()

                            withContext(Dispatchers.Main) {
                                map?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(it.latitude, it.longitude))
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                        .zIndex(1.0f)
                                )
                            }
                        }
                    }

                    else -> null
                }
            }
        }

        sheetViewModel.finishedGame.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    map?.clear()
                    viewModel.notOnRouting()
                    findNavController().navigate(MapFragmentDirections.actionMapFragmentToSuccessDialogFragment())
                }
                //TODO upload userId complete to firebase
            }
        }

        sheetViewModel.imageImageAndLatLngLiveData.observe(viewLifecycleOwner) {
            val pairList = mutableListOf<Pair<String, LatLng>>()
            it.first?.let { first ->
                it.second?.let { second ->
                    first.forEach { image ->
                        second.forEach { market ->
                            // TODO: 判斷此路線的朋友，決定顯示的圖片
                            if (image.pointId == market.marketId) {
                                val url = image.url
                                val latLng = LatLng(market.latitude, market.longitude)

                                val imagePair = Pair(url, latLng)
                                pairList.add(imagePair)
                            }
                        }
                    }
                }
            }

            val newPairList = mutableListOf<Pair<String, LatLng>>()
            for (index in 0 until pairList.size) {
                var count = -1
                for (index2 in index + 1 until pairList.size) {
                    if (pairList[index].second == pairList[index2].second) {
                        count++
                    }
                }
                val oldPair = pairList[index]
                val pair = Pair(oldPair.first,
                    LatLng(oldPair.second.latitude + (count * 0.00005),
                        oldPair.second.longitude + (count * 0.00005)))

                newPairList.add(pair)
            }

            newPairList.forEach {
                glideLoadImageAddMarker(it.first, it.second)
            }
        }

        sheetViewModel.checkUserFinishedLiveData.observe(viewLifecycleOwner) {
            sheetViewModel.checkUserFinished()
        }

        // transformations.map pointsId list from routeData
        sheetViewModel.pointsIdList.observe(viewLifecycleOwner) {
            it?.let {
                sheetViewModel.getPointDetailList(it)
            }
        }

        // use pointId to query pointName LiveData
        sheetViewModel.marketName.observe(viewLifecycleOwner) {
            sheetViewModel.nameAndStateInDataClass(it)
        }

        sheetViewModel.countAndState.observe(viewLifecycleOwner) {
            if (it.first != null && it.second != null) {
                sheetViewModel.countUserImage()
            }
        }

        // TODO: change this observe to friends and mediator images
        sheetViewModel.imagesLiveData.observe(viewLifecycleOwner) {
            sheetViewModel.countFriendsImage("bb11111")
        }

        sheetViewModel.progressLiveData.observe(viewLifecycleOwner) {
            it.first?.let { first ->
                it.second?.let { second ->
                    val mainList = mutableListOf<SheetItem>()
                    first.forEachIndexed { index, marketNameAndState ->
                        val subList = mutableListOf<FriendsProgress>()
                        second.forEach { friend ->
                            if(index.plus(1) == friend.progress) {
                                subList.add(friend)
                            }
                        }
                        if(subList.size > 0) {
                            marketNameAndState.friends = subList
                        }
                        mainList.add(marketNameAndState)
                    }
                    sheetAdapter.submitList(mainList)
                }
            }
        }

//        sheetViewModel.marketNameAndState.observe(viewLifecycleOwner) {
//            sheetAdapter.submitList(it)
//        }

        sheetViewModel.routeDataList.observe(viewLifecycleOwner) {
            it?.let{
                sheetViewModel.routePathsLatLngDrawPolyLine()
            }
        }

        sheetViewModel.routeDetailLatLngList.observe(viewLifecycleOwner) {
            polyline?.remove()
            val lineOption = PolylineOptions()
            lineOption.addAll(it)
            lineOption.width(10f)
            lineOption.color(Color.BLUE)
            lineOption.geodesic(true)
            polyline = map?.addPolyline(lineOption)!!
        }

        sheetViewModel.usersLocationLiveData.observe(viewLifecycleOwner) {
            it.forEach {
                addUserLocationMarkerToMap(LatLng(it.lat.toDouble(), it.lng.toDouble()))
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sheetViewModel.addImageAndLatLngToPair()
        sheetViewModel.addCheckUserFinishedMediator()
        sheetViewModel.addCountAndStateMediatoe()
        sheetViewModel.waitProgressLiveData()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        googleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener)
        googleMap.setOnMyLocationClickListener(onMyLocationClickListener)
        googleMap.setOnPoiClickListener(onPoiClickListener)
        googleMap.setOnMarkerClickListener(setOnMarkerClickListener)
        getLastLocation()
        enableMyLocation()
        startLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.`ACCESS_COARSE_LOCATION`
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
            startLocationUpdates()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
        }
    }

    // autoComplete intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            DESTINATION_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let {
                            val place = Autocomplete.getPlaceFromIntent(data)
                            viewModel.onPlaceSelect(place)
                            Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")
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

            STEP2_DESTINATION_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let {
                            val place = Autocomplete.getPlaceFromIntent(data)
                            viewModel.onPlaceSelect(place)
                            Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")
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

            STEP2_LOCATION_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let {
                            val place = Autocomplete.getPlaceFromIntent(data)
                            Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")
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
                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    val uri: Uri = data?.data!!

                    nextPointId?.let { pointId ->
                        userOnRouteId?.let { routeId ->
                            viewModel.uploadImageUriToStorage(routeId, uri, pointId)
                        }
                    }

                    // Use Uri object instead of File to avoid storage permissions
//                    binding.imgCamera.setImageURI(uri)
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            map?.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ||
            ActivityCompat.shouldShowRequestPermissionRationale(
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
        fastestInterval = 1000
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

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun addMarkersToMap(latLng: LatLng, title: String?) {
        map?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
    }

    private fun addUserLocationMarkerToMap(latLng: LatLng) {
        var userLocationMarker: Marker? = null
        userLocationMarker?.remove()

        userLocationMarker = map?.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        )
    }

    private fun glideLoadImageAddMarker(url: String, latlng: LatLng) {
        lifecycleScope.launch(Dispatchers.IO) {
            val futureTarget = Glide.with(requireContext())
                .asBitmap()
                .load(url)
                .submit(125, 125)
            val bitmap = futureTarget.get()

            withContext(Dispatchers.Main) {
                map!!.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .position(latlng)
                )
            }
            Glide.with(requireContext()).clear(futureTarget)
        }
    }

    private fun moveCameraToMarker(latLng: LatLng) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng, DEFAULT_ZOOM.toFloat()
            )
        )
    }

    private fun setupPlaceApi() {
        // Initialize the SDK
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)

        // Create a new PlacesClient instance
        placesClient = Places.createClient(requireContext())
    }

    // ImagePicker
    private fun startCamera() {
            ImagePicker.with(this)
                .cameraOnly()	//User can only capture image using Camera
                .compress(1024)
                .maxResultSize(620, 620)
                .crop()
                .start(CAMERA_IMAGE_REQ_CODE)
    }


    private val onPoiClickListener = object : GoogleMap.OnPoiClickListener {
        override fun onPoiClick(poi: PointOfInterest) {
            viewModel.onPoiClick(poi)

            Toast.makeText(
                context, """Clicked: ${poi.name}
            Place ID:${poi.placeId}
            Latitude:${poi.latLng.latitude} Longitude:${poi.latLng.longitude}""",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val onMyLocationClickListener = object : GoogleMap.OnMyLocationClickListener {
        override fun onMyLocationClick(location: Location) {
            Toast.makeText(context, "Current location:\n$location", Toast.LENGTH_LONG)
                .show()
        }
    }

    private val onMyLocationButtonClickListener =
        object : GoogleMap.OnMyLocationButtonClickListener {
            override fun onMyLocationButtonClick(): Boolean {
                Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT)
                    .show()
                // Return false so that we don't consume the event and the default behavior still occurs
                // (the camera animates to the user's current position).
                return false
            }
        }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            if (locationResult.locations.size > 0) {
                viewModel.onLocationUpdate(locationResult.locations[0])
            }
        }
    }

    private val setOnMarkerClickListener = object : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(marker: Marker): Boolean {
            viewModel.onMarkerClick(marker)

            Toast.makeText(
                context,
                "${marker.title} has been clicked newClickCount times.",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private val TAG = MapFragment::class.java.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15
        private const val DESTINATION_REQUEST_CODE = 1
        private const val STEP2_DESTINATION_REQUEST_CODE = 2
        private const val STEP2_LOCATION_REQUEST_CODE = 3
        val fields = listOf(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG)
        private const val CAMERA_IMAGE_REQ_CODE = 103
    }
}