package com.yuyu.barhopping.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.lang.UCharacter.getDirection
import android.location.Location
import android.os.AsyncTask
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.gson.Gson
import com.yuyu.barhopping.R
import com.yuyu.barhopping.data.GoogleMapDTO
import com.yuyu.barhopping.databinding.FragmentMapBinding
import com.yuyu.barhopping.util.PermissionUtils
import com.yuyu.barhopping.util.PermissionUtils.isPermissionGranted
import okhttp3.OkHttpClient
import okhttp3.Request

class MapFragment : Fragment(),
    OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var viewModel: MapViewModel

    private var permissionDenied = false
    private var map: GoogleMap? = null
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Obtain the AutocompleteSupportFragment when user search location.
        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(placeSelectionListener)

        // refresh location
        binding.locationBtn.setOnClickListener {
            viewModel.onLocationBtnClick()
        }

        binding.goBtn.setOnClickListener {
            viewModel.getDirection()
            startLocationUpdates()
        }

        viewModel.moveCameraLiveData.observe(
            viewLifecycleOwner, Observer {
                    moveCameraToMarker(it)
            }
        )

        viewModel.desMarkerLiveData.observe(
            viewLifecycleOwner, Observer {
                addMarkersToMap(it)
            }
        )

        viewModel.paths.observe(
            viewLifecycleOwner, Observer {
                val lineOption = PolylineOptions()
                lineOption.addAll(it)
                lineOption.width(10f)
                lineOption.color(Color.BLUE)
                lineOption.geodesic(true)
                map?.addPolyline(lineOption)
            }
        )

        return binding.root
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
        getLastLocation()
        enableMyLocation()
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
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
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
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map?.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
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

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun addMarkersToMap(latLng: LatLng) {
        map?.clear()
        map?.addMarker(
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

    private fun setupPlaceApi() {
        // Initialize the SDK
        Places.initialize(context, getString(R.string.apiKey))

        // Create a new PlacesClient instance
        Places.createClient(context)
    }



    private val onPoiClickListener = object : GoogleMap.OnPoiClickListener {
        override fun onPoiClick(poi: PointOfInterest) {
            viewModel.onPoiClick(poi)
            autocompleteFragment.setText(poi.name)

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

    /**
     * autoCompelete callback
     */
    private val placeSelectionListener = object : PlaceSelectionListener {
        override fun onError(p0: Status) {
            Toast.makeText(context, "PlaceSelectionListener error", Toast.LENGTH_SHORT).show()
        }

        override fun onPlaceSelected(place: Place) {
            viewModel.onPlaceSelect(place)
            Toast.makeText(
                context,
                "Place: ${place.name}, ${place.id}, ${place.latLng}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            if(locationResult.locations.size > 0) {
                viewModel.onLocationUpdate(locationResult.locations[0])
            }
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
    }
}