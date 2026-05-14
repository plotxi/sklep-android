package com.example.sklep.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sklep.R
import com.example.sklep.adapters.ProductAdapter
import com.example.sklep.api.RetrofitClient
import com.example.sklep.models.Product
import com.example.sklep.utils.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

    // Współdzielona lista produktów w koszyku dostępna w całej aplikacji
    companion object {
        val cartItems = mutableListOf<Product>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Konfiguracja listy produktów (RecyclerView)
        val recyclerView = findViewById<RecyclerView>(R.id.rvProducts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Inicjalizacja adaptera z funkcją dodawania do koszyka
        adapter = ProductAdapter(emptyList()) { wybranyProdukt ->
            dodajDoKoszyka(wybranyProdukt)
        }
        recyclerView.adapter = adapter

        // 2. Obsługa przycisku Koszyka (ImageButton)
        val btnGoToCart = findViewById<ImageButton>(R.id.btnGoToCart)
        btnGoToCart.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Twój koszyk jest pusty!", Toast.LENGTH_SHORT).show()
            } else {
                // Otwieramy nową aktywność koszyka
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            }
        }

        // 3. Obsługa przycisku Wyloguj
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            TokenManager.clearToken(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 4. Konfiguracja przycisków kategorii
        setupCategoryButtons()

        // 5. Pobranie produktów z bazy danych
        fetchProducts()
    }

    private fun setupCategoryButtons() {
        findViewById<Button>(R.id.btnAll).setOnClickListener {
            adapter.updateData(allProducts)
        }

        findViewById<Button>(R.id.btnSport).setOnClickListener {
            filterAndCheck(1, "Sport")
        }

        findViewById<Button>(R.id.btnElektronika).setOnClickListener {
            filterAndCheck(2, "Elektronika")
        }

        findViewById<Button>(R.id.btnBooks).setOnClickListener {
            filterAndCheck(3, "Książki")
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            filterAndCheck(4, "Dom")
        }
    }

    private fun dodajDoKoszyka(product: Product) {
        cartItems.add(product)
        Toast.makeText(this, "Dodano do koszyka: ${product.name}", Toast.LENGTH_SHORT).show()
        Log.d("KOSZYK_DEBUG", "Produkty w koszyku: ${cartItems.size}")
    }

    private fun filterAndCheck(id: Int, name: String) {
        val filtered = allProducts.filter { it.category_id == id }
        if (filtered.isEmpty()) {
            Toast.makeText(this, "Brak produktów w kategorii $name", Toast.LENGTH_SHORT).show()
        }
        adapter.updateData(filtered)
    }

    private fun fetchProducts() {
        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getService(this@MainActivity)
                val response = service.getProducts()

                if (response.isSuccessful) {
                    val products = response.body() ?: emptyList()
                    allProducts = products
                    adapter.updateData(products)
                    Log.d("PRODUKTY_DEBUG", "Pobrano: ${products.size} produktów")
                } else {
                    Log.e("PRODUKTY_DEBUG", "Błąd API: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PRODUKTY_DEBUG", "Wyjątek sieci: ${e.message}")
                Toast.makeText(this@MainActivity, "Błąd połączenia z serwerem", Toast.LENGTH_SHORT).show()
            }
        }
    }
}