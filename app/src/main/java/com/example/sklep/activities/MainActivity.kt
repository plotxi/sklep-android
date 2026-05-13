package com.example.sklep.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.sklep.R
import com.example.sklep.api.RetrofitClient
import com.example.sklep.utils.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Definiujemy zmienne dla listy i adaptera
    private lateinit var adapter: ProductAdapter
    private lateinit var rvProducts: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- TWOJA ISTNIEJĄCA LOGIKA WYLOGOWANIA ---
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            TokenManager.clearToken(this)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // --- NOWA LOGIKA: OBSŁUGA PRODUKTÓW ---

        // 1. Znajdujemy RecyclerView w layoucie
        rvProducts = findViewById(R.id.rvProducts)

        // 2. Inicjalizujemy adapter z pustą listą na start
        adapter = ProductAdapter(emptyList())
        rvProducts.adapter = adapter

        // 3. Pobieramy produkty z bazy danych
        fetchProductsFromApi()
    }

    private fun fetchProductsFromApi() {
        lifecycleScope.launch {
            try {
                // Pobieramy instancję serwisu API
                val service = RetrofitClient.getService(this@MainActivity)
                val response = service.getProducts()

                if (response.isSuccessful) {
                    val productsList = response.body() ?: emptyList()

                    // Jeśli lista nie jest pusta, wrzucamy ją do adaptera
                    if (productsList.isNotEmpty()) {
                        adapter.updateData(productsList)
                    } else {
                        Toast.makeText(this@MainActivity, "Brak produktów w bazie", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Serwer zwrócił błąd (np. 404 lub 500)
                    Toast.makeText(this@MainActivity, "Błąd serwera: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Błąd połączenia, np. brak internetu lub zły adres IP
                Toast.makeText(this@MainActivity, "Błąd połączenia: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}