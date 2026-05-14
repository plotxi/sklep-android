package com.example.sklep.models

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,

    // Sprawdź w PHP czy pole to na pewno "image_url" czy "image"
    @SerializedName("image_url")
    val image_url: String,

    // To jest kluczowe dla kategorii!
    // Jeśli w bazie danych pole nazywa się inaczej, wpisz to w nawiasie
    @SerializedName("category_id")
    val category_id: Int
)