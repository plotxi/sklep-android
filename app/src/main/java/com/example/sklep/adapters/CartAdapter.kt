package com.example.sklep.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sklep.R
import com.example.sklep.models.Product

class CartAdapter(private val cartItems: List<Product>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCartProductName)
        val price: TextView = view.findViewById(R.id.tvCartProductPrice)
        val image: ImageView = view.findViewById(R.id.ivCartProductImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = cartItems[position]
        holder.name.text = product.name
        holder.price.text = "${product.price} zł"

        Glide.with(holder.itemView.context)
            .load(product.image_url)
            .into(holder.image)
    }

    override fun getItemCount() = cartItems.size
}