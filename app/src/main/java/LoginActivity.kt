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

        // 1. NAJPIERW ustawiamy widok, aby uniknąć crashu przy starcie
        setContentView(R.layout.activity_login)

        // 2. AUTO-LOGOWANIE: Sprawdzamy token
        val savedToken = TokenManager.getToken(this)
        if (!savedToken.isNullOrEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            // Flagi czyszczą stos, żeby przycisk "wstecz" nie wracał do logowania
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return // Bardzo ważne: kończymy onCreate, jeśli użytkownik jest już zalogowany
        }

        // 3. Inicjalizacja widoków (tylko jeśli nie było autologowania)
        val etLogin = findViewById<TextInputEditText>(R.id.etLogin)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        // Przejście do rejestracji
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Obsługa logowania
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
                                // Zapisujemy token i idziemy do sklepu
                                TokenManager.saveToken(this@LoginActivity, tokenValue)
                                Toast.makeText(this@LoginActivity, "Zalogowano!", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            val msg = body?.message ?: "Błędne dane logowania"
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val msg = if (response.code() == 401) "Nieprawidłowy login lub hasło"
                        else "Błąd serwera: ${response.code()}"
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}