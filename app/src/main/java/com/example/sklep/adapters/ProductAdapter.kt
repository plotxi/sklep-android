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

class ProductAdapter(private var products: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvProductName)
        val price: TextView = view.findViewById(R.id.tvProductPrice)
        val image: ImageView = view.findViewById(R.id.ivProductImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.name.text = product.name
        holder.price.text = "${product.price} zł"

        Glide.with(holder.itemView.context)
            .load(product.image_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.image)
    }

    override fun getItemCount() = products.size

    // Ta funkcja jest kluczowa dla filtrowania!
    fun updateData(newProducts: List<Product>) {
        this.products = newProducts
        notifyDataSetChanged()
    }
}