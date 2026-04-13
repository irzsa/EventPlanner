package com.example.eventplan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

        // 1. Find the Featured UI elements
        val rvFeatured = findViewById<RecyclerView>(R.id.rvFeaturedVendors)
        val tvFeaturedLabel = findViewById<TextView>(R.id.tvFeaturedLabel)

        // Find our other UI elements
        val etSearchName = findViewById<EditText>(R.id.etSearchName)
        val etSearchLocation = findViewById<EditText>(R.id.etSearchLocation)
        val rvCategories = findViewById<RecyclerView>(R.id.rvCategories)
        val rvSearchResults = findViewById<RecyclerView>(R.id.rvSearchResults)

        rvSearchResults.layoutManager = LinearLayoutManager(this)

        // Load Featured Vendors
        RetrofitClient.instance.getFeaturedVendors().enqueue(object : Callback<List<VendorDashboardResponse>> {
            override fun onResponse(call: Call<List<VendorDashboardResponse>>, response: Response<List<VendorDashboardResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    rvFeatured.adapter = FeaturedVendorAdapter(response.body()!!)
                    rvFeatured.visibility = View.VISIBLE
                    tvFeaturedLabel.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<List<VendorDashboardResponse>>, t: Throwable) {
                rvFeatured.visibility = View.GONE
                tvFeaturedLabel.visibility = View.GONE
            }
        })

        // Profile Button Logic
        btnProfile.setOnClickListener {
            val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
            val userRole = sharedPreferences.getString("ROLE", "client")

            if (userRole == "vendor") {
                val intent = Intent(this, VendorProfileActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, ClientProfileActivity::class.java)
                startActivity(intent)
            }
        }

        // --- THE HIDE & SEEK SEARCH LOGIC ---
        fun performSearch() {
            val queryName = etSearchName.text.toString().trim()
            val queryLoc = etSearchLocation.text.toString().trim()

            // If BOTH boxes are empty, bring back the Categories AND Featured Vendors!
            if (queryName.isEmpty() && queryLoc.isEmpty()) {
                rvSearchResults.visibility = View.GONE
                rvCategories.visibility = View.VISIBLE

                // Show Featured
                rvFeatured.visibility = View.VISIBLE
                tvFeaturedLabel.visibility = View.VISIBLE
                return
            }

            // Otherwise, they are searching: Hide Categories and Featured, Show Results!
            rvCategories.visibility = View.GONE

            // Hide Featured
            rvFeatured.visibility = View.GONE
            tvFeaturedLabel.visibility = View.GONE

            // Show Results
            rvSearchResults.visibility = View.VISIBLE

            val apiName = if (queryName.isNotEmpty()) queryName else null
            val apiLoc = if (queryLoc.isNotEmpty()) queryLoc else null

            RetrofitClient.instance.searchVendors(apiName, apiLoc).enqueue(object : Callback<List<VendorDashboardResponse>> {
                override fun onResponse(call: Call<List<VendorDashboardResponse>>, response: Response<List<VendorDashboardResponse>>) {
                    if (response.isSuccessful && response.body() != null) {
                        rvSearchResults.adapter = VendorSearchAdapter(response.body()!!)
                    }
                }
                override fun onFailure(call: Call<List<VendorDashboardResponse>>, t: Throwable) {}
            })
        }

        // --- The "Search-As-You-Type" Listeners ---
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                performSearch()
            }
        }

        etSearchName.addTextChangedListener(textWatcher)
        etSearchLocation.addTextChangedListener(textWatcher)

        // Load Categories
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        RetrofitClient.instance.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful && response.body() != null) {
                    rvCategories.adapter = CategoryAdapter(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Failed to load: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}