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

    // We store the bookings up here so the calendar clicker can access them later!
    private var vendorBookings: List<VendorHubBooking> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_profile)

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // 1. Wipe the vault
            val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            // 2. Teleport back to Login
            val intent = Intent(this, MainActivity::class.java)
            // This flag clears the backstack so they can't hit 'Back' to return to the profile
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val tvName = findViewById<TextView>(R.id.tvHubVendorName)
        val tvDetails = findViewById<TextView>(R.id.tvHubVendorDetails)
        val calendarView = findViewById<CalendarView>(R.id.calendarVendorHub)
        val tvStatus = findViewById<TextView>(R.id.tvSelectedDateStatus)

        // 1. Who is holding the phone?
        val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        if (userId != -1) {
            // 2. Ask Python for the Master Dashboard
            RetrofitClient.instance.getVendorDashboard(userId).enqueue(object : Callback<VendorDashboardResponse> {

                override fun onResponse(call: Call<VendorDashboardResponse>, response: Response<VendorDashboardResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val dashboard = response.body()!!

                        // Paint the business info on the screen
                        tvName.text = dashboard.vendorName
                        tvDetails.text = "${dashboard.location}  •  $${dashboard.pricePerHour}/hr"

                        // Save the bookings silently in the background
                        vendorBookings = dashboard.bookings

                        val btnEdit = findViewById<Button>(R.id.btnEditVendorProfile)
                        btnEdit.setOnClickListener {
                            val intent = Intent(
                                this@VendorProfileActivity,
                                EditVendorActivity::class.java
                            ).apply {
                                putExtra("VENDOR_ID", dashboard.vendorId)
                                putExtra("VENDOR_NAME", dashboard.vendorName)
                                putExtra("VENDOR_PRICE", dashboard.pricePerHour)
                                putExtra("VENDOR_LOC", dashboard.location)
                            }
                            startActivity(intent)
                        }

                    } else {
                        tvName.text = "Vendor profile not found!"
                    }
                }

                override fun onFailure(call: Call<VendorDashboardResponse>, t: Throwable) {
                    Toast.makeText(this@VendorProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // 3. The Magic Calendar Clicker
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            // Fix Android's weird 0-indexed months
            val realMonth = month + 1

            // Format the clicked date to match Python perfectly (e.g., "2026-05-09")
            // The %02d forces single digits to have a leading zero (5 becomes 05)
            val clickedDate = String.format("%04d-%02d-%02d", year, realMonth, dayOfMonth)

            // Search our background list of bookings to see if this date is taken
            val foundBooking = vendorBookings.find { it.bookedDate == clickedDate }

            if (foundBooking != null) {
                // IT'S BOOKED! Turn the box red and show the client.
                tvStatus.text = "🚨 BOOKED\nClient: @${foundBooking.clientUsername}"
                tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFCDD2")) // Light Red
                tvStatus.setTextColor(android.graphics.Color.parseColor("#B71C1C")) // Dark Red
            } else {
                // IT'S FREE! Turn the box green.
                tvStatus.text = "✅ AVAILABLE\nNo bookings for this date."
                tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#C8E6C9")) // Light Green
                tvStatus.setTextColor(android.graphics.Color.parseColor("#1B5E20")) // Dark Green
            }
        }
    }
}