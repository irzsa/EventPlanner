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

        // Find our new UI elements
        val etSearchName = findViewById<android.widget.EditText>(R.id.etSearchName)
        val etSearchLocation = findViewById<android.widget.EditText>(R.id.etSearchLocation)
        val rvCategories = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCategories)
        val rvSearchResults = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSearchResults)

        rvSearchResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        // This is the function that talks to Python
        fun performSearch() {
            val queryName = etSearchName.text.toString().trim()
            val queryLoc = etSearchLocation.text.toString().trim()

            // If BOTH boxes are empty, hide the search results and bring back the Categories!
            if (queryName.isEmpty() && queryLoc.isEmpty()) {
                rvSearchResults.visibility = android.view.View.GONE
                rvCategories.visibility = android.view.View.VISIBLE
                return
            }

            // Otherwise, hide Categories, show Results, and call Python!
            rvCategories.visibility = android.view.View.GONE
            rvSearchResults.visibility = android.view.View.VISIBLE

            // Because our Python endpoint made them Optional, we send null if the box is empty
            val apiName = if (queryName.isNotEmpty()) queryName else null
            val apiLoc = if (queryLoc.isNotEmpty()) queryLoc else null

            RetrofitClient.instance.searchVendors(apiName, apiLoc).enqueue(object : retrofit2.Callback<List<VendorDashboardResponse>> {
                override fun onResponse(call: retrofit2.Call<List<VendorDashboardResponse>>, response: retrofit2.Response<List<VendorDashboardResponse>>) {
                    if (response.isSuccessful && response.body() != null) {
                        // Put the results in our new Adapter
                        rvSearchResults.adapter = VendorSearchAdapter(response.body()!!)
                    }
                }
                override fun onFailure(call: retrofit2.Call<List<VendorDashboardResponse>>, t: Throwable) {
                    // Fail silently so it doesn't spam the user with error toasts while typing
                }
            })
        }

        // --- The "Search-As-You-Type" Listeners ---
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            // This runs the moment they finish pressing a key!
            override fun afterTextChanged(s: android.text.Editable?) {
                performSearch()
            }
        }

        // Attach the listener to both boxes
        etSearchName.addTextChangedListener(textWatcher)
        etSearchLocation.addTextChangedListener(textWatcher)

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