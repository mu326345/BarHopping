package com.yuyu.barhopping.post

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.BarPost
import com.yuyu.barhopping.util.timeNow

class PostViewModel: ViewModel() {

    private val db = Firebase.firestore

    private val storage = Firebase.storage

    val selectPositionName = MutableLiveData<String>()

    val commend = MutableLiveData<String?>()

    var selectPlace: Place ?= null

    var postImage: Uri ?= null

    private val _success = MutableLiveData<Boolean>(false)
    val success: LiveData<Boolean>
        get() = _success

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error


    fun uploadImageToStorage(uri: Uri) {
        val imagesRef = storage.reference.child("post/${uri.lastPathSegment}")
        val uploadTask = imagesRef.putFile(uri)

        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // 獲取下載網址
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imagesRef.downloadUrl

            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    postImage = task.result

                } else {
                    // Handle failures
                }
            }
        }
    }

    fun updatePost() {
        if (UserManager.user != null &&
            commend.value != null &&
            selectPlace != null &&
            postImage != null
        ) {
            val barPostRef = db.collection("BarPost")
                .document()

            val barData = BarPost(
                barPostRef.id,
                UserManager.user!!.id,
                UserManager.user!!.name,
                UserManager.user!!.icon,
                postImage.toString(),
                commend.value!!,
                selectPlace!!.name!!,
                selectPlace!!.id!!,
                selectPlace!!.address ?: null,
                selectPlace!!.latLng?.latitude.toString(),
                selectPlace!!.latLng?.longitude.toString(),
                selectPlace!!.phoneNumber ?: null,
                timeNow()
            )

            barPostRef.set(barData)
            _success.value = true
        } else {
            _error.value = "Please check finish your commend"
        }
    }

    fun selectPosition(place: Place) {
        selectPositionName.value = place.name
        selectPlace = place
    }

    fun onSuccess() {
        _success.value = false
    }
}