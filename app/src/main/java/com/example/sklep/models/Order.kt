package com.example.sklep.models

import com.google.gson.annotations.SerializedName

data class Order(
    val id: Int,
    val total: Double,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    val items: List<OrderProductItem>?
)

data class OrderProductItem(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("product_name")
    val productName: String,
    val quantity: Int,
    val price: Double,
    @SerializedName("image_url")
    val imageUrl: String?
) {
    val lineTotal: Double
        get() = price * quantity
}
