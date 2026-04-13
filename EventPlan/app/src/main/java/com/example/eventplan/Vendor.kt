package com.example.eventplan

import com.google.gson.annotations.SerializedName

data class Vendor(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price_per_hour") val pricePerHour: Double,
    @SerializedName("location") val location: String
)
