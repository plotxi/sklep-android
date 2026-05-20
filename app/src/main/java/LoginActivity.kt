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
import org.json.JSONObject

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
                Toast.makeText(this, "Uzupelnij pola.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!NetworkUtils.hasInternet(this)) {
                Toast.makeText(this, "Brak polaczenia z internetem.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            progressLogin.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.getService(this@LoginActivity)
                        .login(LoginRequest(login = login, email = login, username = login, password = pass))

                    if (response.isSuccessful) {
                        val body = response.body()
                        val tokenValue = body?.data?.token ?: body?.token

                        if (body?.success == true && !tokenValue.isNullOrEmpty()) {
                            TokenManager.saveToken(this@LoginActivity, tokenValue)
                            Toast.makeText(this@LoginActivity, "Zalogowano.", Toast.LENGTH_SHORT).show()
                            openMain()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                body?.message ?: "Bledne dane logowania.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val msg = parseErrorMessage(response.code(), response.errorBody()?.string())
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
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

    private fun parseErrorMessage(code: Int, errorBody: String?): String {
        if (code == 401) {
            return "Nieprawidlowy login lub haslo."
        }

        val message = errorBody
            ?.takeIf { it.isNotBlank() }
            ?.let { body ->
                runCatching { JSONObject(body).optString("message") }.getOrNull()
                    ?.takeIf { it.isNotBlank() }
            }

        if (!message.isNullOrBlank()) {
            return message
        }

        return when (code) {
            400 -> "Nieprawidlowe dane logowania."
            500 -> "Blad serwera logowania. Sprawdz pliki API na hostingu."
            else -> "Nie udalo sie zalogowac. Kod HTTP: $code"
        }
    }

    private fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
