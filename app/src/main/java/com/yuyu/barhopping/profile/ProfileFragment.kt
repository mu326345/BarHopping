package com.yuyu.barhopping.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.R
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.databinding.FragmentProfileBinding
import com.yuyu.barhopping.databinding.FragmentProgressBarBinding
import com.yuyu.barhopping.factory.ViewModelFactory

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel by viewModels<ProfileViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.user = UserManager
        binding.viewModel = viewModel

        return binding.root
    }

}