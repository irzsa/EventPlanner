package com.example.eventplan

import com.google.gson.annotations.SerializedName

// This matches the big package we just built in Python
data class VendorDashboardResponse(
    @SerializedName("vendor_id") val vendorId: Int,
    @SerializedName("vendor_name") val vendorName: String,
    @SerializedName("price_per_hour") val pricePerHour: Double,
    @SerializedName("location") val location: String,
    @SerializedName("bookings") val bookings: List<VendorHubBooking>,
    @SerializedName("description") val description: String?, // NEW
    @SerializedName("image_url") val imageUrl: String?,   // NEW
    @SerializedName("social_link") val socialLink: String? // NEW
)

// A smaller class just for the booking list inside the package
data class VendorHubBooking(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("booked_date") val bookedDate: String,
    @SerializedName("client_username") val clientUsername: String
)