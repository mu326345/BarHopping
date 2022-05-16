package com.yuyu.barhopping.map.finished

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentAllSuccessDialogBinding


class AllSuccessDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentAllSuccessDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAllSuccessDialogBinding.inflate(inflater, container, false)

        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)

        return binding.root
    }

}