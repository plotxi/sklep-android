package com.example.sklep.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sklep.R
import com.example.sklep.api.LoginRequest
import com.example.sklep.api.RetrofitClient
import com.example.sklep.utils.NetworkUtils
import com.example.sklep.utils.TokenManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val savedToken = TokenManager.getToken(this)
        if (!savedToken.isNullOrEmpty()) {
            openMain()
            return
        }

        val etLogin = findViewById<TextInputEditText>(R.id.etLogin)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val progressLogin = findViewById<ProgressBar>(R.id.progressLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (login.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Uzupełnij pola.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!NetworkUtils.hasInternet(this)) {
                Toast.makeText(this, "Brak połączenia z internetem.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            progressLogin.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.getService(this@LoginActivity)
                        .login(LoginRequest(login, pass))

                    if (response.isSuccessful) {
                        val body = response.body()
                        val tokenValue = body?.data?.token

                        if (body?.success == true && !tokenValue.isNullOrEmpty()) {
                            TokenManager.saveToken(this@LoginActivity, tokenValue)
                            Toast.makeText(this@LoginActivity, "Zalogowano.", Toast.LENGTH_SHORT).show()
                            openMain()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                body?.message ?: "Błędne dane logowania.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val msg = if (response.code() == 401) {
                            "Nieprawidłowy login lub hasło."
                        } else {
                            "Nie udało się zalogować. Spróbuj ponownie."
                        }
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, NetworkUtils.friendlyMessage(e), Toast.LENGTH_LONG).show()
                } finally {
                    btnLogin.isEnabled = true
                    progressLogin.visibility = View.GONE
                }
            }
        }
    }

    private fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
