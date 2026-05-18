package com.example.sklep.activities

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sklep.R
import com.example.sklep.api.RegisterRequest
import com.example.sklep.api.RetrofitClient
import com.example.sklep.utils.NetworkUtils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etRegEmail = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etRegLogin = findViewById<TextInputEditText>(R.id.etRegLogin)
        val etRegPassword = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etRegPasswordRepeat = findViewById<TextInputEditText>(R.id.etRegPasswordRepeat)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val progressRegister = findViewById<ProgressBar>(R.id.progressRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        tvBackToLogin.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val email = etRegEmail.text.toString().trim()
            val login = etRegLogin.text.toString().trim()
            val pass = etRegPassword.text.toString().trim()
            val passRepeat = etRegPasswordRepeat.text.toString().trim()

            if (email.isEmpty() || login.isEmpty() || pass.isEmpty() || passRepeat.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola.", Toast.LENGTH_SHORT).show()
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

            if (!NetworkUtils.hasInternet(this)) {
                Toast.makeText(this, "Brak połączenia z internetem.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            progressRegister.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.getService(this@RegisterActivity)
                        .register(RegisterRequest(login, email, pass))

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true) {
                            Toast.makeText(this@RegisterActivity, "Konto utworzone.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                body?.message ?: "Nie udało się utworzyć konta.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            readErrorMessage(response.errorBody()?.string()),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, NetworkUtils.friendlyMessage(e), Toast.LENGTH_LONG).show()
                } finally {
                    btnRegister.isEnabled = true
                    progressRegister.visibility = View.GONE
                }
            }
        }
    }

    private fun readErrorMessage(rawError: String?): String {
        if (rawError.isNullOrBlank()) {
            return "Nie udało się utworzyć konta."
        }

        return try {
            JSONObject(rawError).optString("message", "Nie udało się utworzyć konta.")
        } catch (e: Exception) {
            "Nie udało się utworzyć konta."
        }
    }
}
