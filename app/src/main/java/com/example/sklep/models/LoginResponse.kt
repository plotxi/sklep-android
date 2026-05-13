package com.example.sklep.models

// To musi pasować do Twojego helper.php
data class LoginResponse(
    val success: Boolean,
    val data: LoginData?,    // Tutaj ląduje token z PHP
    val message: String?     // Tutaj ląduje tekst błędu z jsonError
)

data class LoginData(
    val token: String,
    val expires_in: Int
)