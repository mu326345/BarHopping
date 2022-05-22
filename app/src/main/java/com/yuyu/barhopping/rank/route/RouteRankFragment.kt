package com.yuyu.barhopping.rank.route

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuyu.barhopping.data.NewRouteStore
import com.yuyu.barhopping.data.RouteStore
import com.yuyu.barhopping.databinding.FragmentRouteRankBinding

class RouteRankFragment : Fragment() {

    private lateinit var binding: FragmentRouteRankBinding
    private val viewModel by viewModels<RouteRankViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.addCollectionAndRouteToMediator()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRouteRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val recyclerLayout = binding.recyclerLayout
        recyclerLayout.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.recyclerLayout.adapter = RouteRankAdapter(
            RouteRankAdapter.OnClickListener {
                viewModel.navigateToDetail(it)
            })

        viewModel.collectAndRouteMediatorLiveData.observe(viewLifecycleOwner) {
           val list =  it.first?.filterIndexed { index, routeStore ->
               index < 10
           }?.map { NewRouteStore(
               it.id,
               it.startPoint,
               it.startLat,
               it.startLon,
               it.endPoint,
               it.endLat,
               it.endLon,
               it.marketCount,
               it.length,
               it.hardDegree,
               it.comments,
               it.points,
               it.paths,
               it.time,
               it.userName,
               it.userIcon,
               false
           ) }

            it.second?.forEach { collection ->
                list?.forEach { newRouteStore ->
                    if(collection == newRouteStore.id) {
                        newRouteStore.userLike = true
                    }
                }
            }
            it.first?.let {
                (binding.recyclerLayout.adapter as RouteRankAdapter).submitList(list)
            }
        }

        viewModel.navigateToDetail.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(RouteRankFragmentDirections.navigateToRouteDetailFragment(it))
                viewModel.onDetailNavigated()
            }
        }

        return binding.root
    }
}