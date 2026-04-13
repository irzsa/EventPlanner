package com.example.eventplan// KEEP YOUR PACKAGE NAME

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_profile)

        val btnEditProfile = findViewById<Button>(R.id.btnEditClientProfile)
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditClientActivity::class.java)
            startActivity(intent)
        }

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

        val rvClientBookings = findViewById<RecyclerView>(R.id.rvClientBookings)
        rvClientBookings.layoutManager = LinearLayoutManager(this)

        // 1. Get the Client's ID from the Vault
        val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
        val clientId = sharedPreferences.getInt("USER_ID", -1)

        // 2. Fetch their bookings from Python
        if (clientId != -1) {
            RetrofitClient.instance.getClientBookings(clientId).enqueue(object : Callback<List<ClientBooking>> {

                override fun onResponse(call: Call<List<ClientBooking>>, response: Response<List<ClientBooking>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val bookings = response.body()!!

                        if (bookings.isEmpty()) {
                            Toast.makeText(this@ClientProfileActivity, "You have no bookings yet!", Toast.LENGTH_SHORT).show()
                        } else {
                            // We pass in the logic for the Long Click!
                            rvClientBookings.adapter = ClientBookingAdapter(bookings) { bookingIdToDelete ->

                                // --- THE NEW CONFIRMATION POP-UP ---
                                android.app.AlertDialog.Builder(this@ClientProfileActivity)
                                    .setTitle("Cancel Booking")
                                    .setMessage("Are you sure you want to cancel this reservation? This cannot be undone.")
                                    .setPositiveButton("Yes, Cancel") { _, _ ->

                                        // If they click Yes, fire the DELETE request to Python!
                                        RetrofitClient.instance.cancelBooking(bookingIdToDelete).enqueue(object : Callback<Any> {
                                            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                                                if (response.isSuccessful) {
                                                    Toast.makeText(this@ClientProfileActivity, "Booking Cancelled!", Toast.LENGTH_SHORT).show()
                                                    recreate() // Refresh the screen
                                                }
                                            }
                                            override fun onFailure(call: Call<Any>, t: Throwable) {
                                                Toast.makeText(this@ClientProfileActivity, "Error canceling.", Toast.LENGTH_SHORT).show()
                                            }
                                        })

                                    }
                                    .setNegativeButton("No, Keep it", null) // Does nothing, just closes the box
                                    .show()
                                // -----------------------------------
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<ClientBooking>>, t: Throwable) {
                    Toast.makeText(this@ClientProfileActivity, "Failed to load: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}