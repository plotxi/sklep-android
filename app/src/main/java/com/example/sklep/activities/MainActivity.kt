package com.example.sklep.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.rvProducts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = ProductAdapter(emptyList())
        recyclerView.adapter = adapter

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            TokenManager.clearToken(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // --- OBSŁUGA KATEGORII ---

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

        fetchProducts()
    }

    // Pomocnicza funkcja do filtrowania i sprawdzania co jest w liście
    private fun filterAndCheck(id: Int, name: String) {
        val filtered = allProducts.filter { it.category_id == id }
        if (filtered.isEmpty()) {
            Toast.makeText(this, "Brak produktów w kategorii $name (ID: $id)", Toast.LENGTH_SHORT).show()
            // Logujemy ID wszystkich produktów, żebyś wiedział co przychodzi z bazy
            allProducts.forEach { Log.d("DEBUG_KAT", "Produkt: ${it.name}, posiada category_id: ${it.category_id}") }
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
                    Log.d("PRODUKTY_DEBUG", "Pobrano produktów: ${products.size}")
                } else {
                    Log.e("PRODUKTY_DEBUG", "Błąd serwera: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PRODUKTY_DEBUG", "Błąd sieci: ${e.message}")
            }
        }
    }
}