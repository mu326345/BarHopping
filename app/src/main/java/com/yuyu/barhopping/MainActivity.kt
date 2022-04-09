package com.yuyu.barhopping

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.yuyu.barhopping.champ.ChampFragment
import com.yuyu.barhopping.databinding.ActivityMainBinding
import com.yuyu.barhopping.explore.ExploreFragment
import com.yuyu.barhopping.map.MapFragment
import com.yuyu.barhopping.post.PostFragment
import com.yuyu.barhopping.profile.ProfileFragment
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
        val bottomNavView: MeowBottomNavigation = binding.bottomNavView

        bottomNavView.apply {
            addFragment(MapFragment())
            show(R.id.navigation_map, true)

            add(MeowBottomNavigation.Model(R.id.navigation_map, R.drawable.ic_map_24))
            add(MeowBottomNavigation.Model(R.id.navigation_champ, R.drawable.ic_champ_24))
            add(MeowBottomNavigation.Model(R.id.navigation_post, R.drawable.ic_add_24))
            add(MeowBottomNavigation.Model(R.id.navigation_explore, R.drawable.ic_explore_24))
            add(MeowBottomNavigation.Model(R.id.navigation_profile, R.drawable.ic_person_24))
        }

        setupBottomNav()
        setupNavController(navController)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.myNavHostFragment, fragment).addToBackStack(Fragment::class.java.simpleName).commit()
    }

    private fun addFragment(fragment: Fragment) {
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.add(R.id.myNavHostFragment, fragment).addToBackStack(Fragment::class.java.simpleName).commit()
    }

    private fun setupBottomNav() {
        binding.bottomNavView.setOnClickMenuListener { item ->
            when (item.id) {
                R.id.navigation_map -> {

                    replaceFragment(MapFragment())
                }
                R.id.navigation_champ -> {

                    replaceFragment(ChampFragment())
                }
                R.id.navigation_post -> {

                    replaceFragment(PostFragment())
                }
                R.id.navigation_explore -> {

                    replaceFragment(ExploreFragment())
                }
                R.id.navigation_profile -> {

                    replaceFragment(ProfileFragment())
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