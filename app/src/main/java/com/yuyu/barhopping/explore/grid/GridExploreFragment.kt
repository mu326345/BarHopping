package com.yuyu.barhopping.explore.grid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuyu.barhopping.databinding.FragmentGridExploreBinding


class GridExploreFragment : Fragment() {

    private lateinit var binding: FragmentGridExploreBinding
    private lateinit var viewModel: GridExploreViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGridExploreBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this).get(GridExploreViewModel::class.java)

//        val recyclerLayout = binding.recyclerLayout


        return binding.root
    }
}