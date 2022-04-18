package com.yuyu.barhopping.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yuyu.barhopping.BuildConfig
import com.yuyu.barhopping.data.GoogleMapDTO
import com.yuyu.barhopping.data.NearbyData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


private const val BASE_URL = "https://maps.googleapis.com"


private val client = OkHttpClient.Builder()
    .addInterceptor(
        HttpLoggingInterceptor().apply {
            level = when (BuildConfig.DEBUG) {
                true -> HttpLoggingInterceptor.Level.BODY
                false -> HttpLoggingInterceptor.Level.NONE
            }
        }
    )
    .build()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()


interface MapApiService {
    @GET(
        "/maps/api/directions/json?" +
                "sensor=false&mode=walking" +
                "&key="
    )
    suspend fun getDirectionResult(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
    ): GoogleMapDTO

    @GET(
        "/maps/api/place/nearbysearch/json?" +
                "&type=convenience_store" +
                "&key="
    )
    suspend fun getNearbyMarket(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("keyword") keyword: String
    ): NearbyData
}

object DirectionApi {
    val retrofitService: MapApiService by lazy { retrofit.create(MapApiService::class.java) }
}