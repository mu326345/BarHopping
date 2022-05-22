package com.yuyu.barhopping.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.yuyu.barhopping.R
import com.yuyu.barhopping.databinding.FragmentPostBinding
import com.yuyu.barhopping.map.MapFragment


class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
    private val viewModel by viewModels<PostViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPostBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.postImage.setOnClickListener {
            startCamera()
        }

        binding.shareBtn.setOnClickListener {
            viewModel.updatePost()
        }

        binding.addLocationEdit.setOnFocusChangeListener { view, isChange ->
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                MapFragment.fields
            ).build(requireContext())
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        viewModel.success.observe(viewLifecycleOwner) {
            if(it) {
                findNavController().navigate(PostFragmentDirections.navigateToExploreFragment())
                viewModel.onSuccess()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_IMAGE_REQ_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
//                        val uri: Uri = data?.data!!
                        data?.data?.let {
                            glideImage(binding.postImage, it)
                            viewModel.uploadImageToStorage(it)
                        }
                    }
                    ImagePicker.RESULT_ERROR -> {
//                        Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
//                        Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            AUTOCOMPLETE_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let {
                            val place = Autocomplete.getPlaceFromIntent(data)
                            viewModel.selectPosition(place)
                        }
                    }
                    AutocompleteActivity.RESULT_ERROR -> {
                        data?.let {
                            val status = Autocomplete.getStatusFromIntent(data)
                            Log.i(TAG, status.statusMessage ?: "")
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        // The user canceled the operation.
                    }
                }
            }
        }

    }

    private fun startCamera() {
        ImagePicker.with(this)
            .compress(1024)
            .maxResultSize(620, 620)
            .cropSquare()
            .start(CAMERA_IMAGE_REQ_CODE)
    }

    fun glideImage(view: ImageView, uri: Uri) {
        // glide load progress
//        val dra = CircularProgressDrawable(requireContext()).apply {
//            setColorSchemeColors(R.color.coffee_red, com.airbnb.lottie.R.color.primary_dark_material_dark, R.color.coffee_red)
//            setCenterRadius(30f)
//            setStrokeWidth(5f)
//            start()
//        }

        Glide.with(requireContext())
            .asBitmap()
            .load(uri)
//            .placeholder(dra)
            .into(view)

    }

    companion object {
        private val TAG = PostFragment::class.java.simpleName
        private const val CAMERA_IMAGE_REQ_CODE = 103
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }
}