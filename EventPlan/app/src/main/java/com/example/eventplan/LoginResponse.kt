package com.example.eventplan

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("role") val role: String
)