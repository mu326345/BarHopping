package com.yuyu.barhopping.map.finished

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.SuccessDialogFragmentBinding

class SuccessDialogFragment : DialogFragment() {

    private lateinit var binding: SuccessDialogFragmentBinding
    private lateinit var viewModel: SuccessDialogViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = SuccessDialogFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.submitBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        isCancelable = false

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SuccessDialogViewModel::class.java)
    }

}

