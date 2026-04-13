package com.example.eventplan

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EventPlanApi {

    // 1. Your original endpoint
    @GET("categories")
    fun getCategories(): Call<List<Category>>

    // 2. Get vendors for a specific category
    @GET("categories/{category_id}/vendors")
    fun getVendorsForCategory(@Path("category_id") categoryId: Int): Call<List<Vendor>>

    // 3. Get booked dates for a calendar
    @GET("vendors/{vendor_id}/booked-dates")
    fun getVendorBookedDates(@Path("vendor_id") vendorId: Int): Call<List<String>>

    // 4. Create a new booking
    @POST("bookings")
    fun createBooking(@Body request: BookingRequest): Call<Any>
}