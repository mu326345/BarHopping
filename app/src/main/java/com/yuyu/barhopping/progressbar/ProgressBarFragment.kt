package com.yuyu.barhopping.progressbar

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.yuyu.barhopping.databinding.FragmentProgressBarBinding


class ProgressBarFragment : DialogFragment() {

    private lateinit var binding: FragmentProgressBarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = FragmentProgressBarBinding.inflate(inflater, container, false)

        dialog?.setCancelable(false)

        return binding.root
    }
}