package com.yuyu.barhopping

import android.app.Application
import com.yuyu.barhopping.repository.DefaultRepository
import com.yuyu.barhopping.repository.FirebaseRepository
import com.yuyu.barhopping.repository.datasource.FirebaseDataSource

class Application: Application() {

    val repository = DefaultRepository(FirebaseDataSource(this))
}