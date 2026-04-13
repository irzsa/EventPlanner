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
                            // We will use a quick built-in adapter for now!
                            rvClientBookings.adapter = ClientBookingAdapter(bookings)
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