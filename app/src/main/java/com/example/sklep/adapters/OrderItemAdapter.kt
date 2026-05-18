package com.example.sklep.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sklep.R
import com.example.sklep.models.OrderProductItem

class OrderItemAdapter(
    private var items: List<OrderProductItem>
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    class OrderItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivOrderItemImage)
        val name: TextView = view.findViewById(R.id.tvOrderItemName)
        val quantity: TextView = view.findViewById(R.id.tvOrderItemQuantity)
        val total: TextView = view.findViewById(R.id.tvOrderItemTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_product, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.productName
        holder.quantity.text = "x${item.quantity} po %.2f zł".format(item.price)
        holder.total.text = "%.2f zł".format(item.lineTotal)

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.image)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<OrderProductItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
