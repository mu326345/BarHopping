package com.yuyu.barhopping.profile

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.R
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.databinding.FragmentProfileBinding
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
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.user = UserManager
        binding.viewModel = viewModel

        binding.logoutTv.setOnClickListener {
            if (prefs.contains("userId")) {
                prefs.edit().remove("userId").apply()
            }
            findNavController().navigate(R.id.navigate_to_login_fragment)
        }

        return binding.root
    }
}