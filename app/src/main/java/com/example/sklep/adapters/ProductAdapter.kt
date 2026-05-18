package com.example.sklep.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sklep.R
import com.example.sklep.models.Product

class ProductAdapter(
    private var products: List<Product>,
    private val onAddToCartClicked: (Product) -> Unit,
    private val onProductClicked: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvProductName)
        val price: TextView = view.findViewById(R.id.tvProductPrice)
        val image: ImageView = view.findViewById(R.id.ivProductImage)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.name.text = product.name
        holder.price.text = formatPrice(product.price)
        holder.itemView.setOnClickListener {
            onProductClicked(product)
        }

        Glide.with(holder.itemView.context)
            .load(product.image_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.image)

        holder.btnAddToCart.isEnabled = product.stock > 0
        holder.btnAddToCart.text = if (product.stock > 0) {
            "Dodaj do koszyka"
        } else {
            "Brak w magazynie"
        }
        holder.btnAddToCart.setOnClickListener {
            onAddToCartClicked(product)
        }
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) {
            "%.0f zł".format(price)
        } else {
            "%.2f zł".format(price)
        }
    }
}
