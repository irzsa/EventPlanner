package com.example.eventplan

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // 6. Get Client's Bookings
    @GET("clients/{client_id}/bookings")
    fun getClientBookings(@Path("client_id") clientId: Int): Call<List<ClientBooking>>

    // 7. Get Vendor's Master Dashboard
    @GET("users/{user_id}/vendor-dashboard")
    fun getVendorDashboard(@Path("user_id") userId: Int): Call<VendorDashboardResponse>

    // 8. Update Vendor Business Details
    @PUT("vendors/{vendor_id}")
    fun updateVendorDetails(@Path("vendor_id") vendorId: Int, @Body request: VendorUpdateRequest): Call<Any>

    // 9. Update User Profile (Client Name & Phone)
    @PUT("users/{user_id}")
    fun updateUserProfile(@Path("user_id") userId: Int, @Body request: UserUpdateRequest): Call<Any>
}