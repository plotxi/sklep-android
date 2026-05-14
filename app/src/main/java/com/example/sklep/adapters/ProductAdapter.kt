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
    private val onAddToCartClicked: (Product) -> Unit // Callback do obsługi koszyka
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvProductName)
        val price: TextView = view.findViewById(R.id.tvProductPrice)
        val image: ImageView = view.findViewById(R.id.ivProductImage)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart) // Twój nowy przycisk
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.name.text = product.name
        holder.price.text = "${product.price} zł"

        // Ładowanie zdjęcia przez Glide
        Glide.with(holder.itemView.context)
            .load(product.image_url)
            .placeholder(android.R.drawable.ic_menu_gallery) // Obrazek zastępczy
            .error(android.R.drawable.stat_notify_error)    // Obrazek błędu
            .into(holder.image)

        // Obsługa kliknięcia "Dodaj do koszyka"
        holder.btnAddToCart.setOnClickListener {
            onAddToCartClicked(product)
        }
    }

    override fun getItemCount() = products.size

    // Metoda do odświeżania listy (np. przy filtrowaniu kategorii)
    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}