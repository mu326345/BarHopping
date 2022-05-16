package com.yuyu.barhopping.explore.route

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewSwitcher
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.databinding.FragmentRouteExploreBinding
import com.yuyu.barhopping.factory.ViewModelFactory
import javax.inject.Inject


class RouteExploreFragment : Fragment() {

    private lateinit var binding: FragmentRouteExploreBinding
    private lateinit var adapter: RouteExploreAdapter
    private val viewModel by viewModels<RouteExploreViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRouteExploreBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = RouteExploreAdapter()

        val recyclerLayout = binding.recyclerLayout
        binding.recyclerLayout.adapter = adapter
        recyclerLayout.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


        viewModel.routeCommendItem.observe(
            viewLifecycleOwner, Observer {
                it?.let {
                    adapter.submitList(it)
                }
            }
        )

        return binding.root
    }
}