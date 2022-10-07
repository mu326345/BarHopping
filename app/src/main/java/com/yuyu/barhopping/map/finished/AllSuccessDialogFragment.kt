package com.yuyu.barhopping.map.finished

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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

        dialog?.apply {
            setCanceledOnTouchOutside(true)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        return binding.root
    }

}