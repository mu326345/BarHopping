package com.yuyu.barhopping.rank.route.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.yuyu.barhopping.R
import com.yuyu.barhopping.data.PointData
import com.yuyu.barhopping.databinding.FragmentRouteDetailBinding
import com.yuyu.barhopping.util.getVmFactory


class RouteDetailFragment :
    Fragment(),
    OnMapReadyCallback {

    private lateinit var binding: FragmentRouteDetailBinding
    private val viewModel by viewModels<RouteDetailViewModel> {
        getVmFactory(RouteDetailFragmentArgs.fromBundle(requireArguments()).storeKey) }

    private lateinit var map: GoogleMap
    private var polyline: Polyline? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRouteDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        val mapFragment = childFragmentManager.findFragmentById(R.id.detail_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.goNavigate.setOnClickListener {
            viewModel.routeStore.value?.let {
                val result = it
                setFragmentResult("routeIdKey", bundleOf("routeId" to result))
                findNavController().navigate(RouteDetailFragmentDirections.navigateToMapFragment())
            }
        }

        viewModel.currentPointDatas.observe(viewLifecycleOwner) {
            addPointMarkers(it)
        }
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        addMarkers()
        moveCameraToMarker()
        viewModel.getPointDetailList()
    }

    private fun addMarkers() {
        addPathMarkers()
        addOriAndDesMarkers()
    }

    private fun moveCameraToMarker() {
        viewModel.routeStore.value?.let {
            val endLatlng = toLatlng(it.endLat, it.startLon)

            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    endLatlng, DEFAULT_ZOOM.toFloat())
            )
        }
    }

    private fun addPathMarkers() {
        val pathLatlngs = viewModel.getPathLatlngs()

        val lineOption = PolylineOptions()
        lineOption.addAll(pathLatlngs)
        lineOption.width(10f)
        lineOption.color(Color.BLUE)
        lineOption.geodesic(true)
        polyline = map.addPolyline(lineOption)
    }

    private fun addPointMarkers(list: List<PointData>) {
        list.forEach { point ->
            when (point.type) {

                1 -> {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.sevenmarker2))
                            .zIndex(1.0f)
                    )
                }

                2 -> {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.familymarker2))
                            .zIndex(1.0f)
                    )
                }

                3 -> {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.hilifemarker2))
                            .zIndex(1.0f)
                    )
                }

                4 -> {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.okmarker2))
                            .zIndex(1.0f)
                    )
                }

                else -> null
            }
        }
    }

    private fun addOriAndDesMarkers() {
        viewModel.routeStore.value?.let {
            val startLatLng = toLatlng(it.startLat, it.startLon)
            map.addMarker(
                MarkerOptions()
                    .position(startLatLng)
                    .title(it.startPoint)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_circle))
                    .zIndex(1.0f)
            )

            val endLatLng = toLatlng(it.endLat, it.endLon)
            map.addMarker(
                MarkerOptions()
                    .position(endLatLng)
                    .title(it.endPoint)
            )
        }
    }

    private fun toLatlng(lat: String, lng: String): LatLng {
        return LatLng(lat.toDouble(), lng.toDouble())
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
    }
}