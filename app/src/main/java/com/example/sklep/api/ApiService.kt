package com.example.sklep.api

import com.example.sklep.models.Category
import com.example.sklep.models.ApiResponse
import com.example.sklep.models.LoginResponse
import com.example.sklep.models.Product // Pamiętaj o zaimportowaniu modelu Product!
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(
    val login: String,
    val password: String
)

data class RegisterRequest(
    val login: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

interface ApiService {
    @POST("api/auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // Metoda do kategorii
    @GET("api/categories/read.php")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    // NOWA METODA: Pobieranie produktów
    // Założyłem, że plik wrzucisz do api/products/get_products.php
    @GET("api/get_products.php")
    suspend fun getProducts(): Response<List<Product>>
}