package com.example.sklep.activities

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sklep.R
import com.example.sklep.api.RegisterRequest
import com.example.sklep.api.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etRegEmail = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etRegLogin = findViewById<TextInputEditText>(R.id.etRegLogin)
        val etRegPassword = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etRegPasswordRepeat = findViewById<TextInputEditText>(R.id.etRegPasswordRepeat)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {
            // TEST: To musi się pojawić w Logcat (zakładka Debug)
            Log.d("SKLEP_DEBUG", "Kliknięto przycisk rejestracji!")

            val email = etRegEmail.text.toString().trim()
            val login = etRegLogin.text.toString().trim()
            val pass = etRegPassword.text.toString().trim()
            val passRepeat = etRegPasswordRepeat.text.toString().trim()

            if (email.isEmpty() || login.isEmpty() || pass.isEmpty() || passRepeat.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etRegEmail.error = "Błędny format email"
                return@setOnClickListener
            }

            if (pass.length < 6) {
                etRegPassword.error = "Hasło musi mieć min. 6 znaków"
                return@setOnClickListener
            }

            if (pass != passRepeat) {
                etRegPasswordRepeat.error = "Hasła nie są identyczne"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val service = RetrofitClient.getService(this@RegisterActivity)
                    val response = service.register(RegisterRequest(login, email, pass))

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Konto utworzone!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            // Serwer zwrócił 200 OK, ale success: false
                            val msg = body?.message ?: "Ten użytkownik lub email już istnieje"
                            Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Serwer zwrócił kod błędu (np. 400 lub 409 przy zajętym loginie)
                        // Tutaj wyciągamy komunikat "Login jest już zajęty" wysłany z PHP
                        val errorMsg = try {
                            val jObjError =
                                org.json.JSONObject(response.errorBody()?.string() ?: "")
                            jObjError.getString("message")
                        } catch (e: Exception) {
                            "Błąd: Login lub Email może być już zajęty"
                        }

                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Błąd sieci: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            tvBackToLogin.setOnClickListener {
                Log.d("SKLEP_DEBUG", "Powrót do logowania")
                finish()
            }
        }
    }
    }