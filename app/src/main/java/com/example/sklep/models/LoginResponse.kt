package com.example.sklep.models

// To musi pasować do Twojego helper.php
data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val data: LoginData?,
    val message: String?
)

data class LoginData(
    val token: String,
    val expires_in: Int?,
    val user: UserData?
)

data class UserData(
    val id: Int?,
    val login: String?,
    val email: String?,
    val role: String?
)
