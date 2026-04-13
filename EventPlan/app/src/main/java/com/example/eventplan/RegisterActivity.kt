package com.example.eventplan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFullName = findViewById<EditText>(R.id.etRegFullName)
        val etPhone = findViewById<EditText>(R.id.etRegPhone)
        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitRegistration)

        btnSubmit.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Find out which radio button is currently clicked
            val selectedRoleId = rgRole.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.rbVendor) "vendor" else "client"

            // 1. Safety Check
            if (fullName.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Disable button to prevent double-clicking
            btnSubmit.isEnabled = false
            btnSubmit.text = "Creating Account..."

            // 3. Package the data
            val request = RegisterRequest(username, password, role, fullName, phone)

            // 4. Send to Python!
            RetrofitClient.instance.registerUser(request).enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Account Created! Please log in.", Toast.LENGTH_LONG).show()
                        finish() // Closes this screen and drops them back at the Login screen
                    } else {
                        // 🚨 NEW: GRAB THE REAL ERROR FROM PYTHON!
                        val realError = response.errorBody()?.string() ?: "Unknown Error"
                        println("🚨 REGISTRATION FAILED: $realError") // Prints to Logcat

                        // Show it on the screen so you can see it instantly
                        Toast.makeText(this@RegisterActivity, "Error: $realError", Toast.LENGTH_LONG).show()

                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Sign Up"
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Sign Up"
                }
            })
        }
    }
}