package com.yuyu.barhopping.rank.person

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yuyu.barhopping.databinding.FragmentPersonRankBinding

class PersonRankFragment : Fragment() {

    private lateinit var binding: FragmentPersonRankBinding
    private lateinit var viewModel: PersonRankViewModel
    private lateinit var adapter: PersonRankAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPersonRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this).get(PersonRankViewModel::class.java)
        adapter = PersonRankAdapter()

        val recyclerLayout = binding.recyclerLayout
        binding.recyclerLayout.adapter = adapter
        recyclerLayout.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        viewModel.userItem.observe(
            viewLifecycleOwner, Observer {
                adapter.submitList(it)
            }
        )

        return binding.root
    }
}