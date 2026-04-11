package com.example.eventplan

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

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()


            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
            } else {

                val intent = Intent(this, DashboardActivity::class.java)

                startActivity(intent)

                finish()
            }
        }
    }
}