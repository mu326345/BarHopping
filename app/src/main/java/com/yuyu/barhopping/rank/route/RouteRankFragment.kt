package com.yuyu.barhopping.rank.route

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuyu.barhopping.databinding.FragmentRouteRankBinding

class RouteRankFragment : Fragment() {

    private lateinit var binding: FragmentRouteRankBinding
    private lateinit var viewModel: RouteRankViewModel
    private lateinit var adapter: RouteRankAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRouteRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this).get(RouteRankViewModel::class.java)
        adapter = RouteRankAdapter()

        val recyclerLayout = binding.recyclerLayout
        binding.recyclerLayout.adapter = adapter
        recyclerLayout.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


        viewModel.routeItem.observe(
            viewLifecycleOwner, Observer {
                it?.let {
                    adapter.submitList(it)
                }
            }
        )

        return binding.root
    }
}