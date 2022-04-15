package com.yuyu.barhopping.explore.bar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentBarExploreBinding

class BarExploreFragment : Fragment() {
    
    private lateinit var binding: FragmentBarExploreBinding
    private lateinit var viewModel: BarExploreViewModel
    private lateinit var adapter: BarExploreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        binding = FragmentBarExploreBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this).get(BarExploreViewModel::class.java)
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