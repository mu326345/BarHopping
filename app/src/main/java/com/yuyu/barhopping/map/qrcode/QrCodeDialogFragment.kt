package com.yuyu.barhopping.map.qrcode

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.databinding.FragmentQrCodeDialogBinding
import com.yuyu.barhopping.map.MapViewModel


class QrCodeDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentQrCodeDialogBinding
    private val viewModel by viewModels<QrCodeViewModel>()
    private val mapViewModel by viewModels<MapViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentQrCodeDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        UserManager.user?.onRoute?.let {
            binding.qrCodeImg.setImageBitmap(viewModel.getRouteIdQrCodeBitmap(it))
        }

        binding.checkBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}