package com.example.sklep.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sklep.R
import com.example.sklep.adapters.OrderItemAdapter
import com.example.sklep.api.RetrofitClient
import com.example.sklep.models.Order
import com.example.sklep.utils.NetworkUtils
import com.example.sklep.utils.TokenManager
import kotlinx.coroutines.launch

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var adapter: OrderItemAdapter
    private lateinit var tvNumber: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTotal: TextView
    private lateinit var progress: View
    private lateinit var btnRetry: Button
    private var orderId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        findViewById<ImageButton>(R.id.btnOrderDetailBack).setOnClickListener { finish() }

        tvNumber = findViewById(R.id.tvOrderDetailNumber)
        tvDate = findViewById(R.id.tvOrderDetailDate)
        tvStatus = findViewById(R.id.tvOrderDetailStatus)
        tvTotal = findViewById(R.id.tvOrderDetailTotal)
        progress = findViewById(R.id.progressOrderDetail)
        btnRetry = findViewById(R.id.btnRetryOrderDetail)

        adapter = OrderItemAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rvOrderProducts).apply {
            layoutManager = LinearLayoutManager(this@OrderDetailActivity)
            adapter = this@OrderDetailActivity.adapter
        }

        orderId = intent.getIntExtra(EXTRA_ORDER_ID, 0)
        if (orderId <= 0) {
            Toast.makeText(this, "Nieprawidłowe zamówienie", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnRetry.setOnClickListener { fetchOrder(orderId) }
        fetchOrder(orderId)
    }

    private fun fetchOrder(orderId: Int) {
        if (!NetworkUtils.hasInternet(this)) {
            showLoadError("Brak połączenia z internetem.")
            return
        }

        progress.visibility = View.VISIBLE
        btnRetry.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getService(this@OrderDetailActivity).getOrder(orderId)
                when {
                    response.isSuccessful -> {
                        val order = response.body()?.data
                        if (order != null) {
                            showOrder(order)
                        } else {
                            showLoadError("Nie znaleziono zamówienia.")
                        }
                    }
                    response.code() == 401 -> handleUnauthorized()
                    else -> showLoadError("Nie udało się pobrać szczegółów zamówienia.")
                }
            } catch (e: Exception) {
                showLoadError(NetworkUtils.friendlyMessage(e))
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun showOrder(order: Order) {
        btnRetry.visibility = View.GONE
        tvNumber.text = "Zamówienie #${order.id}"
        tvDate.text = order.createdAt
        tvStatus.text = statusLabel(order.status)
        tvTotal.text = "Suma: %.2f zł".format(order.total)
        adapter.updateData(order.items ?: emptyList())
    }

    private fun showLoadError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        btnRetry.visibility = View.VISIBLE
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

    private fun handleUnauthorized() {
        TokenManager.clearToken(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
    }
}
