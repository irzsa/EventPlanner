package com.example.eventplan

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VendorDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_details)

        // Unpack the Intent extras
        val vendorId = intent.getIntExtra("VENDOR_ID", -1)
        val name = intent.getStringExtra("VENDOR_NAME")
        val desc = intent.getStringExtra("VENDOR_DESC")
        val price = intent.getDoubleExtra("VENDOR_PRICE", 0.0)
        val loc = intent.getStringExtra("VENDOR_LOC")

        // Bind them to the UI
        findViewById<TextView>(R.id.tvDetailName).text = name
        findViewById<TextView>(R.id.tvDetailLocPrice).text = "$loc  •  $$price/hr"
        findViewById<TextView>(R.id.tvDetailDesc).text = desc

        val btnBook = findViewById<Button>(R.id.btnBookCalendar)
        btnBook.setOnClickListener {
            Toast.makeText(this, "Opening Calendar for ID: $vendorId", Toast.LENGTH_SHORT).show()
            // Next up: Build the calendar bottom sheet or swap the UI to show dates!
        }
    }
}