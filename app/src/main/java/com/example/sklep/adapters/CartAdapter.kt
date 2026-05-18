package com.example.sklep.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sklep.R
import com.example.sklep.utils.CartItem

class CartAdapter(
    private val cartItems: List<CartItem>,
    private val onIncreaseClicked: (CartItem) -> Unit,
    private val onDecreaseClicked: (CartItem) -> Unit,
    private val onRemoveClicked: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCartProductName)
        val price: TextView = view.findViewById(R.id.tvCartProductPrice)
        val quantity: TextView = view.findViewById(R.id.tvCartProductQuantity)
        val image: ImageView = view.findViewById(R.id.ivCartProductImage)
        val btnDecrease: ImageButton = view.findViewById(R.id.btnDecreaseQuantity)
        val btnIncrease: ImageButton = view.findViewById(R.id.btnIncreaseQuantity)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveCartItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        val product = item.product

        holder.name.text = product.name
        holder.price.text = "%.2f zł".format(item.lineTotal)
        holder.quantity.text = item.quantity.toString()
        holder.btnIncrease.isEnabled = item.quantity < product.stock

        holder.btnIncrease.setOnClickListener { onIncreaseClicked(item) }
        holder.btnDecrease.setOnClickListener { onDecreaseClicked(item) }
        holder.btnRemove.setOnClickListener { onRemoveClicked(item) }

        Glide.with(holder.itemView.context)
            .load(product.image_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.image)
    }

    override fun getItemCount() = cartItems.size
}
