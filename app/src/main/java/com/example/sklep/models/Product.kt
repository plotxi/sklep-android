package com.example.sklep.models

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double,

    @SerializedName("image_url")
    val image_url: String?,

    @SerializedName("category_id")
    val category_id: Int,

    val stock: Int
)
