package com.example.eventplan

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone_number") val phoneNumber: String
)