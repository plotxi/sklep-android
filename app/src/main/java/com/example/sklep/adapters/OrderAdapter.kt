package com.example.sklep.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sklep.R
import com.example.sklep.models.Order

class OrderAdapter(
    private var orders: List<Order>,
    private val onOrderClicked: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.tvOrderNumber)
        val date: TextView = view.findViewById(R.id.tvOrderDate)
        val total: TextView = view.findViewById(R.id.tvOrderTotal)
        val status: TextView = view.findViewById(R.id.tvOrderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.number.text = "Zamówienie #${order.id}"
        holder.date.text = order.createdAt
        holder.total.text = "%.2f zł".format(order.total)
        holder.status.text = statusLabel(order.status)
        holder.status.setBackgroundColor(statusColor(order.status))
        holder.itemView.setOnClickListener { onOrderClicked(order) }
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    private fun statusLabel(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "Oczekuje"
            "paid" -> "Opłacone"
            "shipped" -> "Wysłane"
            "cancelled" -> "Anulowane"
            else -> status
        }
    }

    private fun statusColor(status: String): Int {
        return when (status.lowercase()) {
            "pending" -> Color.rgb(245, 158, 11)
            "paid" -> Color.rgb(34, 197, 94)
            "shipped" -> Color.rgb(59, 130, 246)
            "cancelled" -> Color.rgb(185, 28, 28)
            else -> Color.rgb(103, 80, 164)
        }
    }
}
