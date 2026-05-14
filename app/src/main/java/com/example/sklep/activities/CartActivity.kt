package com.example.sklep.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sklep.R
import com.example.sklep.adapters.CartAdapter

class CartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // 1. Obsługa powrotu
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // 2. Konfiguracja listy produktów w koszyku
        val rvCart = findViewById<RecyclerView>(R.id.rvCartItems)
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = CartAdapter(MainActivity.cartItems)

        // 3. Obliczanie sumy
        val tvTotal = findViewById<TextView>(R.id.tvTotalAmount)
        val sum = MainActivity.cartItems.sumOf { it.price.toDouble() }
        tvTotal.text = "Suma: %.2f zł".format(sum)
    }
}