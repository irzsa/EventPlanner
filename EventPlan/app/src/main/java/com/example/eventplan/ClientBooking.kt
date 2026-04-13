package com.example.eventplan

import com.google.gson.annotations.SerializedName

data class ClientBooking(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("booked_date") val bookedDate: String,
    @SerializedName("vendor_name") val vendorName: String,
    @SerializedName("location") val location: String
)
