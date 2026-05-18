package com.example.sklep.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sklep.R
import com.example.sklep.adapters.CartAdapter
import com.example.sklep.api.CreateOrderRequest
import com.example.sklep.api.OrderItemRequest
import com.example.sklep.api.RetrofitClient
import com.example.sklep.utils.CartItem
import com.example.sklep.utils.CartManager
import com.example.sklep.utils.NetworkUtils
import com.example.sklep.utils.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    private lateinit var adapter: CartAdapter
    private lateinit var rvCartItems: RecyclerView
    private lateinit var emptyCartState: LinearLayout
    private lateinit var layoutSummary: LinearLayout
    private lateinit var tvTotalAmount: TextView
    private lateinit var btnPlaceOrder: Button
    private lateinit var progressOrder: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnBrowseProducts).setOnClickListener { finish() }

        rvCartItems = findViewById(R.id.rvCartItems)
        emptyCartState = findViewById(R.id.emptyCartState)
        layoutSummary = findViewById(R.id.layoutSummary)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        progressOrder = findViewById(R.id.progressOrder)

        adapter = CartAdapter(
            CartManager.items(),
            onIncreaseClicked = { item -> increaseQuantity(item) },
            onDecreaseClicked = { item -> decreaseQuantity(item) },
            onRemoveClicked = { item -> removeItem(item) }
        )

        rvCartItems.layoutManager = LinearLayoutManager(this)
        rvCartItems.adapter = adapter

        btnPlaceOrder.setOnClickListener {
            showOrderConfirmation()
        }

        refreshCart()
    }

    private fun increaseQuantity(item: CartItem) {
        val updated = CartManager.updateQuantity(item.product.id, item.quantity + 1)
        if (!updated) {
            Toast.makeText(this, "Nie ma więcej sztuk w magazynie", Toast.LENGTH_SHORT).show()
        }
        refreshCart()
    }

    private fun decreaseQuantity(item: CartItem) {
        CartManager.updateQuantity(item.product.id, item.quantity - 1)
        refreshCart()
    }

    private fun removeItem(item: CartItem) {
        CartManager.removeItem(item.product.id)
        refreshCart()
    }

    private fun showOrderConfirmation() {
        if (CartManager.isEmpty()) {
            refreshCart()
            return
        }

        val summary = buildString {
            CartManager.items().forEach { item ->
                append("${item.product.name} x${item.quantity} - ")
                append("%.2f zł".format(item.lineTotal))
                append("\n")
            }
            append("\nRazem: %.2f zł".format(CartManager.getTotal()))
        }

        AlertDialog.Builder(this)
            .setTitle("Potwierdź zamówienie")
            .setMessage(summary)
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Złóż zamówienie") { _, _ -> createOrder() }
            .show()
    }

    private fun createOrder() {
        if (!NetworkUtils.hasInternet(this)) {
            Toast.makeText(this, "Brak połączenia z internetem.", Toast.LENGTH_LONG).show()
            return
        }

        val items = CartManager.items().map { item ->
            OrderItemRequest(
                product_id = item.product.id,
                quantity = item.quantity
            )
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getService(this@CartActivity)
                val response = service.createOrder(CreateOrderRequest(items))

                when {
                    response.isSuccessful -> {
                        val order = response.body()?.data
                        if (order != null) {
                            CartManager.clear()
                            refreshCart()
                            showOrderSuccess(order.order_id, order.total)
                        } else {
                            Toast.makeText(
                                this@CartActivity,
                                "Zamówienie utworzone, ale brakuje danych odpowiedzi",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    response.code() == 401 -> handleUnauthorized()
                    else -> {
                        Toast.makeText(
                            this@CartActivity,
                            readErrorMessage(response.errorBody()?.string()),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@CartActivity, NetworkUtils.friendlyMessage(e), Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showOrderSuccess(orderId: Int, total: Double) {
        AlertDialog.Builder(this)
            .setTitle("Zamówienie złożone")
            .setMessage("Numer zamówienia: $orderId\nKwota: %.2f zł".format(total))
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun handleUnauthorized() {
        TokenManager.clearToken(this)
        Toast.makeText(this, "Sesja wygasła. Zaloguj się ponownie.", Toast.LENGTH_LONG).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun readErrorMessage(rawError: String?): String {
        if (rawError.isNullOrBlank()) {
            return "Nie udało się złożyć zamówienia"
        }

        return try {
            JSONObject(rawError).optString("message", "Nie udało się złożyć zamówienia")
        } catch (e: Exception) {
            "Nie udało się złożyć zamówienia"
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressOrder.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnPlaceOrder.isEnabled = !isLoading
    }

    private fun refreshCart() {
        adapter.notifyDataSetChanged()
        tvTotalAmount.text = "Suma: %.2f zł".format(CartManager.getTotal())

        val isEmpty = CartManager.isEmpty()
        rvCartItems.visibility = if (isEmpty) View.GONE else View.VISIBLE
        layoutSummary.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyCartState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}
