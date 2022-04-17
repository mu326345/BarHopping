package com.yuyu.barhopping.explore.bar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentBarExploreBinding
import com.yuyu.barhopping.explore.route.RouteExploreViewModel
import com.yuyu.barhopping.factory.ViewModelFactory
import com.yuyu.barhopping.repository.FirebaseRepository

class BarExploreFragment : Fragment() {
    
    private lateinit var binding: FragmentBarExploreBinding
    private lateinit var adapter: BarExploreAdapter
    private val viewModel by viewModels<BarExploreViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        binding = FragmentBarExploreBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = BarExploreAdapter()

        val recyclerLayout = binding.recyclerLayout
        binding.recyclerLayout.adapter = adapter
        recyclerLayout.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


        viewModel.barItem.observe(
            viewLifecycleOwner, Observer {
                adapter.submitList(it)
            }
        )

        return binding.root
    }
}