package com.example.eventplan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VendorProfileActivity : AppCompatActivity() {

    private var vendorBookings: List<VendorHubBooking> = emptyList()
    // We store this globally so the "Edit" button can always grab the LATEST data
    private var currentDashboard: VendorDashboardResponse? = null

    // We define our views at the class level so we can use them in multiple functions
    private lateinit var tvName: TextView
    private lateinit var tvDetails: TextView
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_profile)

        // 1. Initialize Views
        tvName = findViewById(R.id.tvHubVendorName)
        tvDetails = findViewById(R.id.tvHubVendorDetails)
        tvStatus = findViewById(R.id.tvSelectedDateStatus)
        val calendarView = findViewById<CalendarView>(R.id.calendarVendorHub)
        val btnEdit = findViewById<Button>(R.id.btnEditVendorProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 2. Logout Logic (Stays in onCreate)
        btnLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 3. Edit Button Logic
        btnEdit.setOnClickListener {
            currentDashboard?.let { dashboard ->
                println("CHECKING HUB DATA: Desc=${dashboard.description}, Image=${dashboard.imageUrl}")
                val intent = Intent(this@VendorProfileActivity, EditVendorActivity::class.java).apply {
                    putExtra("VENDOR_ID", dashboard.vendorId)
                    putExtra("VENDOR_NAME", dashboard.vendorName)
                    putExtra("VENDOR_PRICE", dashboard.pricePerHour)
                    putExtra("VENDOR_LOC", dashboard.location)
                    putExtra("VENDOR_DESC", dashboard.description)
                    putExtra("VENDOR_IMAGE", dashboard.imageUrl)
                    putExtra("VENDOR_SOCIAL", dashboard.socialLink)
                }
                startActivity(intent)
            } ?: Toast.makeText(this, "Data still loading...", Toast.LENGTH_SHORT).show()
        }

        // 4. Calendar Logic (Stays in onCreate)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val realMonth = month + 1
            val clickedDate = String.format("%04d-%02d-%02d", year, realMonth, dayOfMonth)
            val foundBooking = vendorBookings.find { it.bookedDate == clickedDate }

            if (foundBooking != null) {
                tvStatus.text = "🚨 BOOKED\nClient: @${foundBooking.clientUsername}"
                tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFCDD2"))
                tvStatus.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
            } else {
                tvStatus.text = "✅ AVAILABLE\nNo bookings for this date."
                tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#C8E6C9"))
                tvStatus.setTextColor(android.graphics.Color.parseColor("#1B5E20"))
            }
        }
    }

    // --- THE MAGIC REFRESH PART ---

    override fun onResume() {
        super.onResume()
        // Every time the user comes back to this screen, we ask Python for fresh data!
        refreshDashboard()
    }

    private fun refreshDashboard() {
        val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        if (userId != -1) {
            RetrofitClient.instance.getVendorDashboard(userId).enqueue(object : Callback<VendorDashboardResponse> {
                override fun onResponse(call: Call<VendorDashboardResponse>, response: Response<VendorDashboardResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        // Store the NEW data globally
                        currentDashboard = response.body()

                        // Update the screen text
                        tvName.text = currentDashboard!!.vendorName
                        tvDetails.text = "${currentDashboard!!.location}  •  $${currentDashboard!!.pricePerHour}/hr"
                        vendorBookings = currentDashboard!!.bookings
                    } else {
                        tvName.text = "Profile Error"
                    }
                }

                override fun onFailure(call: Call<VendorDashboardResponse>, t: Throwable) {
                    Toast.makeText(this@VendorProfileActivity, "Refresh Failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}