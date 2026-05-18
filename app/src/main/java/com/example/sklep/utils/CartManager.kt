package com.example.sklep.utils

import com.example.sklep.models.Product

data class CartItem(
    val product: Product,
    var quantity: Int
) {
    val lineTotal: Double
        get() = product.price * quantity
}

object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    fun addItem(product: Product): Boolean {
        if (product.stock <= 0) {
            return false
        }

        val existingItem = cartItems.firstOrNull { it.product.id == product.id }
        if (existingItem != null) {
            return updateQuantity(product.id, existingItem.quantity + 1)
        }

        cartItems.add(CartItem(product, 1))
        return true
    }

    fun add(product: Product): Boolean = addItem(product)

    fun removeItem(productId: Int) {
        cartItems.removeAll { it.product.id == productId }
    }

    fun updateQuantity(productId: Int, quantity: Int): Boolean {
        val item = cartItems.firstOrNull { it.product.id == productId } ?: return false

        if (quantity <= 0) {
            removeItem(productId)
            return true
        }

        if (quantity > item.product.stock) {
            return false
        }

        item.quantity = quantity
        return true
    }

    fun isEmpty(): Boolean = cartItems.isEmpty()

    fun items(): List<CartItem> = cartItems

    fun getTotal(): Double = cartItems.sumOf { it.lineTotal }

    fun total(): Double = getTotal()

    fun size(): Int = cartItems.sumOf { it.quantity }

    fun clear() {
        cartItems.clear()
    }
}
