package com.example.sklep.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.sklep.R
import com.example.sklep.adapters.ProductAdapter
import com.example.sklep.api.RetrofitClient
import com.example.sklep.models.Category
import com.example.sklep.models.Product
import com.example.sklep.utils.CartManager
import com.example.sklep.utils.NetworkUtils
import com.example.sklep.utils.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ProductAdapter
    private lateinit var swipeProducts: SwipeRefreshLayout
    private lateinit var btnRetryProducts: Button
    private var allProducts: List<Product> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeProducts = findViewById(R.id.swipeProducts)
        swipeProducts.setColorSchemeResources(android.R.color.holo_purple)
        swipeProducts.setOnRefreshListener {
            refreshCatalog()
        }
        btnRetryProducts = findViewById(R.id.btnRetryProducts)
        btnRetryProducts.setOnClickListener {
            refreshCatalog()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvProducts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = ProductAdapter(
            emptyList(),
            onAddToCartClicked = { selectedProduct -> addToCart(selectedProduct) },
            onProductClicked = { selectedProduct -> openProductDetails(selectedProduct.id) }
        )
        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.btnGoToCart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnHomeTab).setOnClickListener {
            adapter.updateData(allProducts)
        }

        findViewById<ImageButton>(R.id.btnOrdersTab).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            TokenManager.clearToken(this)
            CartManager.clear()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupCategoryButtons(emptyList())
        refreshCatalog()
        updateCartBadge()
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
        if (::adapter.isInitialized) {
            fetchProducts()
        }
    }

    private fun refreshCatalog() {
        fetchCategories()
        fetchProducts()
    }

    private fun setupCategoryButtons(categories: List<Category>) {
        val container = findViewById<LinearLayout>(R.id.categoryContainer)
        container.removeAllViews()

        container.addView(createCategoryButton("Wszystkie") {
            adapter.updateData(allProducts)
        })

        categories.forEach { category ->
            container.addView(createCategoryButton(category.name) {
                filterAndCheck(category.id, category.name)
            })
        }
    }

    private fun createCategoryButton(text: String, onClick: () -> Unit): Button {
        val marginEnd = (8 * resources.displayMetrics.density).toInt()
        val buttonContext = ContextThemeWrapper(
            this,
            com.google.android.material.R.style.Widget_MaterialComponents_Button_TextButton
        )

        return Button(buttonContext).apply {
            this.text = text
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                this.marginEnd = marginEnd
            }
            setOnClickListener { onClick() }
        }
    }

    private fun addToCart(product: Product) {
        val added = CartManager.add(product)
        if (added) {
            Toast.makeText(this, "Dodano do koszyka: ${product.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Nie można dodać więcej sztuk niż jest w magazynie", Toast.LENGTH_SHORT).show()
        }
        Log.d("KOSZYK_DEBUG", "Produkty w koszyku: ${CartManager.size()}")
        updateCartBadge()
    }

    private fun updateCartBadge() {
        val badge = findViewById<TextView>(R.id.tvCartBadge)
        val count = CartManager.size()

        if (count > 0) {
            badge.text = if (count > 99) "99+" else count.toString()
            badge.visibility = View.VISIBLE
        } else {
            badge.visibility = View.GONE
        }
    }

    private fun openProductDetails(productId: Int) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, productId)
        startActivity(intent)
    }

    private fun filterAndCheck(id: Int, name: String) {
        val filtered = allProducts.filter { it.category_id == id }
        if (filtered.isEmpty()) {
            Toast.makeText(this, "Brak produktów w kategorii $name", Toast.LENGTH_SHORT).show()
        }
        adapter.updateData(filtered)
    }

    private fun fetchProducts() {
        if (!NetworkUtils.hasInternet(this)) {
            showCatalogError("Brak połączenia z internetem.")
            swipeProducts.isRefreshing = false
            return
        }

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getService(this@MainActivity)
                val response = service.getProducts()

                if (response.isSuccessful) {
                    val products = response.body() ?: emptyList()
                    allProducts = products
                    adapter.updateData(products)
                    btnRetryProducts.visibility = View.GONE
                    swipeProducts.visibility = View.VISIBLE
                    Log.d("PRODUKTY_DEBUG", "Pobrano: ${products.size} produktów")
                } else {
                    showCatalogError("Nie udało się pobrać produktów.")
                    Log.e("PRODUKTY_DEBUG", "Błąd API: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PRODUKTY_DEBUG", "Wyjątek sieci: ${e.message}")
                showCatalogError(NetworkUtils.friendlyMessage(e))
            } finally {
                if (::swipeProducts.isInitialized) {
                    swipeProducts.isRefreshing = false
                }
            }
        }
    }

    private fun fetchCategories() {
        if (!NetworkUtils.hasInternet(this)) {
            return
        }

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getService(this@MainActivity)
                val response = service.getCategories()

                if (response.isSuccessful) {
                    val categories = response.body()?.data ?: emptyList()
                    setupCategoryButtons(categories)
                    Log.d("KATEGORIE_DEBUG", "Pobrano: ${categories.size} kategorii")
                } else {
                    Log.e("KATEGORIE_DEBUG", "Błąd API: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("KATEGORIE_DEBUG", "Wyjątek sieci: ${e.message}")
            }
        }
    }

    private fun showCatalogError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        if (allProducts.isEmpty()) {
            swipeProducts.visibility = View.GONE
            btnRetryProducts.visibility = View.VISIBLE
        }
    }
}
