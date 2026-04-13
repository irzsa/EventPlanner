package com.example.eventplan

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<android.widget.TextView>(R.id.tvGoToRegister)
        tvRegister.setOnClickListener {
            val intent = android.content.Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val username = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop here if empty
            }

            // Create the request payload
            val loginData = LoginRequest(username, password)

            // Make the network call to Python!
            RetrofitClient.instance.login(loginData).enqueue(object : Callback<LoginResponse> {

                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginReply = response.body()!!

                        // --- THE VAULT (SharedPreferences) ---
                        // Open the secret notepad on the phone
                        val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()

                        // Jot down the ID and Role
                        editor.putInt("USER_ID", loginReply.userId)
                        editor.putString("ROLE", loginReply.role)
                        editor.apply() // Save it!
                        // -------------------------------------

                        Toast.makeText(this@MainActivity, "Welcome, ${loginReply.fullName}!", Toast.LENGTH_SHORT).show()

                        // Teleport to the Dashboard
                        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish() // This prevents the user from hitting "Back" to go to the login screen

                    } else {
                        // Python responded, but with an error (like a 401 Unauthorized)
                        Toast.makeText(this@MainActivity, "Invalid credentials!", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // The server is down or the IP is wrong
                    Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}