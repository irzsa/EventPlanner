package com.example.eventplan

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val message: String,
    @SerializedName("user_id") val userId: Int,
    val role: String,
    @SerializedName("full_name") val fullName: String // 🚨 Add this!
)