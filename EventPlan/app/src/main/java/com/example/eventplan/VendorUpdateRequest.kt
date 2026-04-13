package com.example.eventplan

import com.google.gson.annotations.SerializedName

data class VendorUpdateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price_per_hour") val pricePerHour: Double,
    @SerializedName("location") val location: String,
    @SerializedName("image_url") val imageUrl: String?,   // NEW
@SerializedName("social_link") val socialLink: String? // NEW
)