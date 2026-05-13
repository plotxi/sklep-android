package com.example.sklep.models

// Generyczna odpowiedź z PHP (Punkt 01)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class LoginRequest(val login: String, val password: String)
data class Category(val id: Int, val name: String)
