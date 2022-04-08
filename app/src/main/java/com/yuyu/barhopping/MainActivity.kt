package com.yuyu.barhopping

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yuyu.barhopping.databinding.ActivityMainBinding
import com.yuyu.barhopping.util.CurrentFragmentType

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        // show bottom navigation
        val navController = binding.myNavHostFragment.getFragment<NavHostFragment>().navController
        val bottomNavView: BottomNavigationView = binding.bottomNavView
        bottomNavView.setupWithNavController(navController)

        setupBottomNav(navController)
        setupNavController(navController)
    }

    private fun setupBottomNav(navController: NavController) {
        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_map -> {

                    navController.navigate(NavigationDirections.navigateToMapFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_champ -> {

                    navController.navigate(NavigationDirections.navigateToChampFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_post -> {

                    navController.navigate(NavigationDirections.navigateToPostFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_explore -> {

                    navController.navigate(NavigationDirections.navigateToExploreFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_profile -> {

                    navController.navigate(NavigationDirections.navigateToProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    /** Set up to record the current fragment */
    private fun setupNavController(navController: NavController) {
        navController.addOnDestinationChangedListener { navController: NavController, _: NavDestination, _: Bundle? ->
            viewModel.currentFragmentType.value = when (navController.currentDestination?.id) {
                R.id.mapFragment -> CurrentFragmentType.MAP
                R.id.champFragment -> CurrentFragmentType.CHAMP
                R.id.postFragment -> CurrentFragmentType.POST
                R.id.exploreFragment -> CurrentFragmentType.EXPLORE
                R.id.profileFragment -> CurrentFragmentType.PROFILE
                else -> viewModel.currentFragmentType.value
            }
        }
    }
}