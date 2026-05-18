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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.sklep.R
import com.example.sklep.adapters.OrderAdapter
import com.example.sklep.api.RetrofitClient
import com.example.sklep.utils.NetworkUtils
import com.example.sklep.utils.TokenManager
import kotlinx.coroutines.launch

class OrdersActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: OrderAdapter
    private lateinit var emptyOrders: TextView
    private lateinit var rvOrders: RecyclerView
    private lateinit var btnRetryOrders: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        findViewById<ImageButton>(R.id.btnOrdersBack).setOnClickListener { finish() }

        emptyOrders = findViewById(R.id.tvEmptyOrders)
        rvOrders = findViewById(R.id.rvOrders)
        btnRetryOrders = findViewById(R.id.btnRetryOrders)
        swipeRefresh = findViewById(R.id.swipeOrders)

        adapter = OrderAdapter(emptyList()) { order ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.id)
            startActivity(intent)
        }

        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = adapter

        btnRetryOrders.setOnClickListener { fetchOrders() }
        swipeRefresh.setColorSchemeResources(android.R.color.holo_purple)
        swipeRefresh.setOnRefreshListener { fetchOrders() }

        fetchOrders()
    }

    override fun onResume() {
        super.onResume()
        fetchOrders()
    }

    private fun fetchOrders() {
        if (!NetworkUtils.hasInternet(this)) {
            showLoadError("Brak połączenia z internetem.")
            return
        }

        swipeRefresh.isRefreshing = true
        btnRetryOrders.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getService(this@OrdersActivity).getOrders()
                when {
                    response.isSuccessful -> {
                        val orders = response.body()?.data ?: emptyList()
                        adapter.updateData(orders)
                        emptyOrders.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                        rvOrders.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
                    }
                    response.code() == 401 -> handleUnauthorized()
                    else -> showLoadError("Nie udało się pobrać historii zamówień.")
                }
            } catch (e: Exception) {
                showLoadError(NetworkUtils.friendlyMessage(e))
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showLoadError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        if (adapter.itemCount == 0) {
            rvOrders.visibility = View.GONE
            emptyOrders.visibility = View.GONE
            btnRetryOrders.visibility = View.VISIBLE
        }
        swipeRefresh.isRefreshing = false
    }

    private fun handleUnauthorized() {
        TokenManager.clearToken(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
