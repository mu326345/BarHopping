package com.yuyu.barhopping.explore.cluster

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.MainActivity
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentClusterBinding
import com.yuyu.barhopping.factory.ViewModelFactory
import com.yuyu.barhopping.util.MarkerItem


class ClusterFragment :
    Fragment(),
    OnMapReadyCallback {

    private lateinit var binding: FragmentClusterBinding
    private val viewModel by viewModels<ClusterViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }

    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<MarkerItem>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).animateShowBottomNav(null)

        binding = FragmentClusterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.cluster_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.exploreMapBtn.setOnClickListener {
            findNavController().navigate(ClusterFragmentDirections.navigateToExploreFragment())
        }

        viewModel.barItem.observe(viewLifecycleOwner) {
            it.forEach {
                it.barName?.let { name ->

                    addItems(it.barLat.toDouble(), it.barLng.toDouble(), name)
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startLocationUpdates()
        clusterManager = ClusterManager(requireContext(), map)
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

    fun createLocationRequest() = LocationRequest.create()?.apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            if (locationResult.locations.size > 0) {
                val location = locationResult.locations[0]
                setUpClusterer(LatLng(location.latitude, location.longitude))
            }
        }
    }

    private fun setUpClusterer(currentLatlng: LatLng) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng, 15f))
            map.setOnCameraIdleListener(clusterManager)
            map.setOnMarkerClickListener(clusterManager)
    }

    fun addItems(lat: Double, lng: Double, name: String) {
        val offsetItem =
            MarkerItem(lat, lng, name, null)
        clusterManager.addItem(offsetItem)
        clusterManager.setAnimation(true)
    }
}