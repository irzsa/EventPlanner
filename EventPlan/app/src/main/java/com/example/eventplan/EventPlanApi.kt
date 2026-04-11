package com.example.eventplan

import retrofit2.Call
import retrofit2.http.GET

interface EventPlanApi {
    // This perfectly matches your Python @app.get("/categories")
    @GET("categories")
    fun getCategories(): Call<List<Category>>
}