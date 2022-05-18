package com.yuyu.barhopping.rank.route

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuyu.barhopping.NavigationDirections
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentRouteRankBinding

class RouteRankFragment : Fragment() {

    private lateinit var binding: FragmentRouteRankBinding
    private lateinit var viewModel: RouteRankViewModel
    private lateinit var adapter: RouteRankAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRouteRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this).get(RouteRankViewModel::class.java)
        binding.viewModel = viewModel

        val recyclerLayout = binding.recyclerLayout
        recyclerLayout.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.recyclerLayout.adapter = RouteRankAdapter(
            RouteRankAdapter.OnClickListener {
            viewModel.navigateToDetail(it)
        })

        viewModel.navigateToDetail.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(RouteRankFragmentDirections.navigateToRouteDetailFragment(it))
                viewModel.onDetailNavigated()
            }
        }

        viewModel.routeItem.observe(
            viewLifecycleOwner, Observer {
                it?.let {
                    (binding.recyclerLayout.adapter as RouteRankAdapter).submitList(it)
                }
            }
        )

        return binding.root
    }
}