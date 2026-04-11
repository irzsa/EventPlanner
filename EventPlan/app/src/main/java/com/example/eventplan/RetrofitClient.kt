package com.example.eventplan

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // The IP address for Android emulators to reach your local Docker server
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val instance: EventPlanApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(EventPlanApi::class.java)
    }
}