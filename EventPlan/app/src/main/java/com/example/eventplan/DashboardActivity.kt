package com.example.eventplan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnProfile = findViewById<Button>(R.id.btnProfile)

        btnProfile.setOnClickListener {
            // 1. Open the vault
            val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)

            // 2. Read the role (default to "client" just in case it's missing)
            val userRole = sharedPreferences.getString("ROLE", "client")

            // 3. The Crossroad!
            if (userRole == "vendor") {
                Toast.makeText(this, "Opening Vendor Hub...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, VendorProfileActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Opening Client Hub...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ClientProfileActivity::class.java)
                startActivity(intent)
            }
        }

        val rvCategories = findViewById<RecyclerView>(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Make the network call to your Python server!
        RetrofitClient.instance.getCategories().enqueue(object : Callback<List<Category>> {

            // If the server responds successfully
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful && response.body() != null) {
                    val realCategories = response.body()!!

                    // We need to extract just the names (Strings) to pass to your existing Adapter

                    // Attach the adapter with the REAL data
                    rvCategories.adapter = CategoryAdapter(realCategories)
                }
            }

            // If the server is down or the IP is wrong
            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Failed to load: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}