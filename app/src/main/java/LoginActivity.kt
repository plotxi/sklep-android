package com.example.sklep.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sklep.R
import com.example.sklep.api.LoginRequest
import com.example.sklep.api.RetrofitClient
import com.example.sklep.utils.TokenManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. AUTO-LOGOWANIE: Sprawdź czy user ma już token
        val savedToken = TokenManager.getToken(this)
        if (!savedToken.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return // Kończymy tutaj, nie ładujemy reszty widoków
        }

        setContentView(R.layout.activity_login)

        val etLogin = findViewById<TextInputEditText>(R.id.etLogin)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        // Przejście do rejestracji
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Logika logowania
        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (login.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Uzupełnij pola!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val service = RetrofitClient.getService(this@LoginActivity)
                    val response = service.login(LoginRequest(login, pass))

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true) {
                            val tokenValue = body.data?.token
                            if (!tokenValue.isNullOrEmpty()) {
                                TokenManager.saveToken(this@LoginActivity, tokenValue)
                                Toast.makeText(this@LoginActivity, "Zalogowano!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            val msg = body?.message ?: "Błędne dane logowania"
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Obsługa błędów statusu HTTP (np. 401 przy złym haśle)
                        val msg = if (response.code() == 401) "Nieprawidłowy login lub hasło"
                        else "Błąd: ${response.code()}"
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}