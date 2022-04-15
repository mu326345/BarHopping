package com.yuyu.barhopping.rank

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yuyu.barhopping.rank.person.PersonRankFragment
import com.yuyu.barhopping.rank.route.RouteRankFragment

class RankAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount() = RankTypeFilter.values().size

    override fun createFragment(position: Int): Fragment {
        return when(RankTypeFilter.values()[position]) {
            RankTypeFilter.ROUTE -> RouteRankFragment()
            else -> PersonRankFragment()
        }
    }
}