package com.yuyu.barhopping

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.yuyu.barhopping.databinding.ActivityMainBinding
import com.yuyu.barhopping.map.MapFragment
import com.yuyu.barhopping.map.qrcode.QrCodeScannerFragment
import com.yuyu.barhopping.map.qrcode.QrCodeScannerFragmentDirections
import com.yuyu.barhopping.util.CurrentFragmentType

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        // show bottom navigation
        val navController = binding.myNavHostFragment.getFragment<NavHostFragment>().navController
        val bottomNavView: MeowBottomNavigation = binding.bottomNavView

        bottomNavView.apply {

            add(MeowBottomNavigation.Model(CurrentFragmentType.MAP.index, R.drawable.ic_map_24))
            add(MeowBottomNavigation.Model(CurrentFragmentType.RANK.index, R.drawable.ic_champ_24))
            add(MeowBottomNavigation.Model(CurrentFragmentType.POST.index, R.drawable.ic_add_24))
            add(MeowBottomNavigation.Model(CurrentFragmentType.EXPLORE.index, R.drawable.ic_explore_24))
            add(MeowBottomNavigation.Model(CurrentFragmentType.PROFILE.index, R.drawable.ic_person_24))
            show(CurrentFragmentType.MAP.index, false)
        }

        setupBottomNav(navController)
        setupNavController(navController)
    }

    private fun setupBottomNav(navController: NavController) {
        binding.bottomNavView.setOnClickMenuListener { item ->
            when (item.id) {
                CurrentFragmentType.MAP.index -> {
                    navController.navigate(NavigationDirections.navigateToMapFragment())
                }
                CurrentFragmentType.RANK.index -> {
                    navController.navigate(NavigationDirections.navigateToChampFragment())
                }
                CurrentFragmentType.POST.index -> {
                    navController.navigate(NavigationDirections.navigateToPostFragment())
                }
                CurrentFragmentType.EXPLORE.index -> {
                    navController.navigate(NavigationDirections.navigateToExploreFragment())
                }
                CurrentFragmentType.PROFILE.index -> {
                    navController.navigate(NavigationDirections.navigateToProfileFragment())
                }
            }
        }
    }

    /** Set up to record the current fragment */
    private fun setupNavController(navController: NavController) {
        navController.addOnDestinationChangedListener { navController: NavController, _: NavDestination, _: Bundle? ->
            viewModel.currentFragmentType.value = when (navController.currentDestination?.id) {
                R.id.mapFragment -> CurrentFragmentType.MAP
                R.id.champFragment -> CurrentFragmentType.RANK
                R.id.postFragment -> CurrentFragmentType.POST
                R.id.exploreFragment -> CurrentFragmentType.EXPLORE
                R.id.profileFragment -> CurrentFragmentType.PROFILE
                else -> viewModel.currentFragmentType.value
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            1 -> {
                val newArr  = permissions.map{ it }
                val navHostFragment = binding.myNavHostFragment.getFragment<NavHostFragment>()
                val list = navHostFragment.childFragmentManager.fragments
                for (f in list) {
                    if (f is MapFragment) {
                        f.onRequestPermissionsResult(requestCode, newArr.toTypedArray(), grantResults)
                    }
                }
            }
            987 -> { // x camera
                val newArr  = permissions.map{ it }
                val navHostFragment = binding.myNavHostFragment.getFragment<NavHostFragment>()
                val list = navHostFragment.childFragmentManager.fragments
                for (f in list) {
                    if (f is QrCodeScannerFragment) {
                        f.onRequestPermissionsResult(requestCode, newArr.toTypedArray(), grantResults)
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun hideBottomNav() {
        binding.bottomNavView.visibility = View.GONE
    }

    fun showBottomNav() {
        binding.bottomNavView.visibility = View.VISIBLE
    }
}