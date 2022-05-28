package com.yuyu.barhopping.explore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentExploreBinding
import com.yuyu.barhopping.factory.ViewModelFactory


class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding
    private lateinit var adapter: ExploreAdapter
    private val viewModel by viewModels<ExploreViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentExploreBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = ExploreAdapter()

        val recyclerLayout = binding.recyclerLayout
        binding.recyclerLayout.adapter = adapter
        recyclerLayout.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.clusterMapBtn.setOnClickListener {
            findNavController().navigate(ExploreFragmentDirections.navigateToClusterFragment())
        }

        viewModel.barItem.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return binding.root
    }
}