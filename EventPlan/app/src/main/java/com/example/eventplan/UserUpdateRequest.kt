package com.example.eventplan

import com.google.gson.annotations.SerializedName

data class UserUpdateRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone_number") val phoneNumber: String
)