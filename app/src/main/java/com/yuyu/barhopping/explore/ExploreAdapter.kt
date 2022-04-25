package com.yuyu.barhopping.explore

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yuyu.barhopping.explore.bar.BarExploreFragment
import com.yuyu.barhopping.explore.route.RouteExploreFragment

class ExploreAdapter(fragment: ExploreFragment): FragmentStateAdapter(fragment) {
    override fun getItemCount() = ExploreTypeFilter.values().size

    override fun createFragment(position: Int): Fragment {
        return when(ExploreTypeFilter.values()[position]) {
            ExploreTypeFilter.ROUTE -> RouteExploreFragment()
            else -> BarExploreFragment()
        }
    }
}