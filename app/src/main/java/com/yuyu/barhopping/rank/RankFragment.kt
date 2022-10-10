package com.yuyu.barhopping.rank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentRankBinding

class RankFragment : Fragment() {

    private lateinit var binding: FragmentRankBinding
    private lateinit var rankAdapter: RankAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        rankAdapter = RankAdapter(this)
        viewPager = binding.viewpagerRank
        viewPager.adapter = rankAdapter

        // Rank viewpager title
        val tabLayout = binding.tabsRank
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(RankTypeFilter.values()[position]) {
                RankTypeFilter.ROUTE -> {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.marqueeTv.isSelected = true
    }

    private val tabIcon = listOf(
        R.drawable.ic_baseline_directions_run_24,
        R.drawable.ic_baseline_military_tech_24
    )

    private val tabTitle = listOf(
        R.string.route,
        R.string.person
    )
}