package com.yuyu.barhopping.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyu.barhopping.UserManager
import com.yuyu.barhopping.data.User
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class GoogleLoginViewModel(val repository: FirebaseRepository) : ViewModel() {

    private val db = Firebase.firestore

    private val _navigateToMap = MutableLiveData<Boolean>()
    val navigateToMap: LiveData<Boolean>
        get() = _navigateToMap

    fun checkUser(userId: String, user:User) {
        db.collection("User")
            .whereEqualTo(FieldPath.documentId(), userId)
            .get()
            .addOnSuccessListener {
                if(it.documents.size > 0) {
                    Log.v("yy", "使用者已經存在")

                    val user = it.documents[0].toObject(User::class.java)
                    Log.v("yy", "user = $user")
                    UserManager.user = user
                    _navigateToMap.value = true

                } else {
                    Log.v("yy", "使用者不存在")
                    addUser(userId, user)
                }
            }
            .addOnFailureListener {
                Log.v("yy", "登入失敗 ＝ ${it.message}")
            }
    }

//    fun getUserDetail(userId: String) {
//        repository.getUserDetail(object : FirebaseDataSource.UserCallBack {
//            override fun onResult(user: User) {
//                UserManager.user = user
//            }
//        }, userId)
//    }

    fun addUser(userId: String, user: User) {
        db.collection("User")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.v("yy", "添加成功")
                UserManager.user = user
                _navigateToMap.value = true
            }
            .addOnFailureListener {
                Log.v("yy", "添加失敗")
            }
    }

    fun navigateNull() {
        _navigateToMap.value = null
    }
}