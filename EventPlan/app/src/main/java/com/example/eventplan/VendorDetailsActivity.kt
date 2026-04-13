package com.example.eventplan

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VendorDetailsActivity : AppCompatActivity() {

    private var bookedDates: List<String> = emptyList()
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_details)

        // 1. Grab the Vendor ID from the previous screen
        val vendorId = intent.getIntExtra("VENDOR_ID", -1)

        // UI Elements
        findViewById<TextView>(R.id.tvDetailName).text = intent.getStringExtra("VENDOR_NAME")
        findViewById<TextView>(R.id.tvDetailLocPrice).text = "${intent.getStringExtra("VENDOR_LOC")}  •  $${intent.getDoubleExtra("VENDOR_PRICE", 0.0)}/hr"
        findViewById<TextView>(R.id.tvDetailDesc).text = intent.getStringExtra("VENDOR_DESC")

        val btnReveal = findViewById<Button>(R.id.btnRevealCalendar)
        val layoutBooking = findViewById<LinearLayout>(R.id.layoutBookingSection)
        val calendar = findViewById<CalendarView>(R.id.calendarClientBooking)
        calendar.minDate = System.currentTimeMillis() - 1000
        val tvStatus = findViewById<TextView>(R.id.tvBookingStatus)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmBooking)

        // 2. Open the vault to get the Client's ID
        val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
        val clientId = sharedPreferences.getInt("USER_ID", -1)

        // 3. Fetch unavailable dates in the background!
        if (vendorId != -1) {
            RetrofitClient.instance.getVendorBookedDates(vendorId).enqueue(object : Callback<List<String>> {
                override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    if (response.isSuccessful && response.body() != null) {
                        bookedDates = response.body()!!
                    }
                }
                override fun onFailure(call: Call<List<String>>, t: Throwable) {
                    Toast.makeText(this@VendorDetailsActivity, "Failed to load calendar data.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 4. Reveal the Calendar when clicked
        btnReveal.setOnClickListener {
            btnReveal.visibility = View.GONE
            layoutBooking.visibility = View.VISIBLE
        }

        // 5. Calendar Click Listener (The Safety Check)
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Format to perfectly match Python's YYYY-MM-DD
            val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            selectedDate = formattedDate

            if (bookedDates.contains(formattedDate)) {
                tvStatus.text = "🚨 This date is already booked!"
                tvStatus.setTextColor(android.graphics.Color.RED)
                btnConfirm.isEnabled = false // Lock the button!
            } else {
                tvStatus.text = "✅ Available! Tap Confirm to book."
                tvStatus.setTextColor(android.graphics.Color.parseColor("#388E3C")) // Green
                btnConfirm.isEnabled = true // Unlock the button!
            }
        }

        // 6. Send the Booking to Python!
        btnConfirm.setOnClickListener {
            if (clientId == -1 || selectedDate == null) return@setOnClickListener

            btnConfirm.isEnabled = false
            btnConfirm.text = "Booking..."

            val request = BookingRequest(vendorId, clientId, selectedDate!!)

            RetrofitClient.instance.createBooking(request).enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@VendorDetailsActivity, "Booking Confirmed!", Toast.LENGTH_LONG).show()
                        finish() // Send them back to the vendor list
                    } else {
                        Toast.makeText(this@VendorDetailsActivity, "Failed to book. It may have just been taken!", Toast.LENGTH_LONG).show()
                        btnConfirm.isEnabled = true
                        btnConfirm.text = "Confirm Booking"
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Toast.makeText(this@VendorDetailsActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Confirm Booking"
                }
            })
        }
    }
}