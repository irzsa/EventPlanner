package com.example.eventplan

import com.google.gson.annotations.SerializedName

data class BookingRequest(
    @SerializedName("vendor_id") val vendorId: Int,
    @SerializedName("booked_date") val bookedDate: String
)
