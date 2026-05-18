package com.example.sklep.api

import com.example.sklep.models.ApiResponse
import com.example.sklep.models.Category
import com.example.sklep.models.LoginResponse
import com.example.sklep.models.Order
import com.example.sklep.models.Product
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

data class OrderItemRequest(
    val product_id: Int,
    val quantity: Int
)

data class CreateOrderRequest(
    val items: List<OrderItemRequest>
)

data class CreateOrderData(
    val order_id: Int,
    val total: Double
)

interface ApiService {
    @POST("api/auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/categories.php")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    @GET("api/get_products.php")
    suspend fun getProducts(): Response<List<Product>>

    @GET("api/products.php")
    suspend fun getProduct(@Query("id") productId: Int): Response<ApiResponse<Product>>

    @POST("api/orders.php")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<CreateOrderData>>

    @GET("api/orders.php")
    suspend fun getOrders(): Response<ApiResponse<List<Order>>>

    @GET("api/orders.php")
    suspend fun getOrder(@Query("id") orderId: Int): Response<ApiResponse<Order>>
}
