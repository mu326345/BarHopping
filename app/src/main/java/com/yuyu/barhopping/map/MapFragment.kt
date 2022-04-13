package com.yuyu.barhopping.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentMapBinding
import com.yuyu.barhopping.util.PermissionUtils
import com.yuyu.barhopping.util.PermissionUtils.isPermissionGranted

class MapFragment : Fragment(),
    OnMapReadyCallback,
    GoogleMap.OnPoiClickListener,
    ActivityCompat.OnRequestPermissionsResultCallback,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMyLocationButtonClickListener,
    PlaceSelectionListener {
//    GoogleApiClient.ConnectionCallbacks,
//    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: FragmentMapBinding

    private var permissionDenied = false
    private lateinit var map: GoogleMap
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // The entry point to the Places API.
    private lateinit var placesClient: PlacesClient
    private val defaultLocation = LatLng(24.00432669769251, 121.54043510577722) // Taiwan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        setupPlaceApi()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Obtain the AutocompleteSupportFragment when user search location.
        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(this)

        // refresh location
        binding.locationBtn.setOnClickListener {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        lastLocation.latitude,
                        lastLocation.longitude
                    ), 15f
                )
            )
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false

        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        map.setOnPoiClickListener(this)
        getLastLocation()
        enableMyLocation()
        getDeviceLocation()
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
            map.isMyLocationEnabled = true
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
                    lastLocation = location
                    val currentLatLong = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 15f))
                }
            }
    }

    private fun addMarkersToMap(latitude: Double, longitude: Double) {
        map.clear()

        val markerLatLng = LatLng(latitude, longitude)
        map.addMarker(
            MarkerOptions()
                .position(markerLatLng)
//                .title("Melbourne")
//                .snippet("Population: 4,137,400")
        )
    }

    private fun setupPlaceApi() {
        // Initialize the SDK
        Places.initialize(context, getString(R.string.apiKey))

        // Create a new PlacesClient instance
        Places.createClient(context)
    }

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (permissionDenied) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastLocation = task.result
                        if (lastLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastLocation!!.latitude,
                                        lastLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    override fun onMyLocationClick(location: Location) {
        Toast.makeText(context, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
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

    override fun onPoiClick(poi: PointOfInterest) {
        addMarkersToMap(poi.latLng.latitude, poi.latLng.longitude)

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    poi.latLng.latitude,
                    poi.latLng.longitude
                ), 15f
            )
        )
        autocompleteFragment.setText(poi.name)

        Toast.makeText(
            context, """Clicked: ${poi.name}
            Place ID:${poi.placeId}
            Latitude:${poi.latLng.latitude} Longitude:${poi.latLng.longitude}""",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onError(status: Status) {
        Toast.makeText(context, "An error occurred: $status", Toast.LENGTH_SHORT).show()
    }

    override fun onPlaceSelected(place: Place) {
        Toast.makeText(context, "Place: ${place.name}, ${place.id}, ${place.latLng}", Toast.LENGTH_SHORT).show()
        map.clear()

        map.addMarker(
            MarkerOptions()
                .position(place.latLng))

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    place.latLng.latitude,
                    place.latLng.longitude
                ), 15f
            )
        )
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

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }

}