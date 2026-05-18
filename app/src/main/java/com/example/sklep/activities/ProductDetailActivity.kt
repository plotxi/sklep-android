package com.example.sklep.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sklep.R
import com.example.sklep.api.RetrofitClient
import com.example.sklep.models.Product
import com.example.sklep.utils.CartManager
import com.example.sklep.utils.NetworkUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var ivProductImage: ImageView
    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvProductDescription: TextView
    private lateinit var tvProductStock: TextView
    private lateinit var btnAddToCart: Button
    private lateinit var btnRetry: Button
    private lateinit var progress: ProgressBar
    private lateinit var content: ScrollView
    private var productId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbarProductDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Szczegóły produktu"

        ivProductImage = findViewById(R.id.ivProductDetailImage)
        tvProductName = findViewById(R.id.tvProductDetailName)
        tvProductPrice = findViewById(R.id.tvProductDetailPrice)
        tvProductDescription = findViewById(R.id.tvProductDetailDescription)
        tvProductStock = findViewById(R.id.tvProductDetailStock)
        btnAddToCart = findViewById(R.id.btnProductDetailAddToCart)
        btnRetry = findViewById(R.id.btnRetryProductDetail)
        progress = findViewById(R.id.progressProductDetail)
        content = findViewById(R.id.productDetailContent)

        productId = intent.getIntExtra(EXTRA_PRODUCT_ID, 0)
        if (productId <= 0) {
            Toast.makeText(this, "Nieprawidłowy produkt", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnRetry.setOnClickListener { fetchProduct(productId) }
        fetchProduct(productId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchProduct(productId: Int) {
        if (!NetworkUtils.hasInternet(this)) {
            showLoadError("Brak połączenia z internetem.")
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getService(this@ProductDetailActivity).getProduct(productId)

                if (response.isSuccessful) {
                    val product = response.body()?.data
                    if (product != null) {
                        showProduct(product)
                    } else {
                        showLoadError("Nie znaleziono produktu.")
                    }
                } else {
                    showLoadError("Nie udało się pobrać produktu.")
                }
            } catch (e: Exception) {
                showLoadError(NetworkUtils.friendlyMessage(e))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showProduct(product: Product) {
        content.visibility = View.VISIBLE
        btnRetry.visibility = View.GONE

        tvProductName.text = product.name
        tvProductPrice.text = "%.2f zł".format(product.price)
        tvProductDescription.text = product.description?.takeIf { it.isNotBlank() }
            ?: "Brak opisu produktu."
        tvProductStock.text = if (product.stock > 0) {
            "W magazynie: ${product.stock} szt."
        } else {
            "Brak w magazynie"
        }

        Glide.with(this)
            .load(product.image_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(ivProductImage)

        btnAddToCart.isEnabled = product.stock > 0
        btnAddToCart.text = if (product.stock > 0) "Dodaj do koszyka" else "Brak w magazynie"
        btnAddToCart.setOnClickListener {
            val added = CartManager.add(product)
            val message = if (added) {
                "Dodano do koszyka: ${product.name}"
            } else {
                "Nie można dodać więcej sztuk niż jest w magazynie"
            }

            Snackbar.make(findViewById(R.id.productDetailRoot), message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progress.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            btnRetry.visibility = View.GONE
        }
    }

    private fun showLoadError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        content.visibility = View.GONE
        btnRetry.visibility = View.VISIBLE
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
    }
}
