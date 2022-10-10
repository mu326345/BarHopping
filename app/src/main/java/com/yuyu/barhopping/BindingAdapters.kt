package com.yuyu.barhopping

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Uses the Glide library to load an image by URL into an [ImageView]
 */
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = it.toUri().buildUpon().build()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.bar_hopping_logo)
                    .error(R.drawable.bar_hopping_logo)
            )
            .into(imgView)
    }
}

@BindingAdapter("length")
fun bindLength(textView: TextView, length: Long) {
    textView.text = "${length}公尺"
}

@BindingAdapter("store")
fun bindStore(textView: TextView, store: Long) {
    textView.text = "${store}間"
}

@BindingAdapter("store")
fun bindStore(textView: TextView, store: Int) {
    textView.text = "${store}間"
}