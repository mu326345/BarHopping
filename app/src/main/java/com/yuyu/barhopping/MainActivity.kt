package com.yuyu.barhopping

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.yuyu.barhopping.databinding.ActivityMainBinding
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
            add(MeowBottomNavigation.Model(CurrentFragmentType.CHAMP.index, R.drawable.ic_champ_24))
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
                CurrentFragmentType.CHAMP.index -> {
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
                R.id.champFragment -> CurrentFragmentType.CHAMP
                R.id.postFragment -> CurrentFragmentType.POST
                R.id.exploreFragment -> CurrentFragmentType.EXPLORE
                R.id.profileFragment -> CurrentFragmentType.PROFILE
                else -> viewModel.currentFragmentType.value
            }
        }
    }
}