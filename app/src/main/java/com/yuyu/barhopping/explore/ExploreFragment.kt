package com.yuyu.barhopping.explore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentExploreBinding


class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding
    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentExploreBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        exploreAdapter = ExploreAdapter(this)
        viewPager = binding.viewpagerExplore
        viewPager.adapter = exploreAdapter


        // explore viewpager title
        val tabLayout = binding.tabsExplore
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(ExploreTypeFilter.values()[position]) {
                ExploreTypeFilter.ROUTE -> {
                    tab.setIcon(tabIcon[0])
                    tab.text = resources.getString(tabTitle[0])
                }
                else -> {
                    tab.setIcon(tabIcon[1])
                    tab.text = resources.getString(tabTitle[1])
                }
            }
        }.attach()


        return binding.root
    }

    private val tabIcon = listOf(
        R.drawable.ic_baseline_directions_run_24,
        R.drawable.ic_baseline_sports_bar_24
    )

    private val tabTitle = listOf(
        R.string.route,
        R.string.bar
    )
}