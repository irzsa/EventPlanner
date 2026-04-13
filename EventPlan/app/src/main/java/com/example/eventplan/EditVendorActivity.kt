package com.example.eventplan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditVendorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_vendor)

        println("DEBUG_DATA: Desc: ${intent.getStringExtra("VENDOR_DESC")}")

        // 1. Initialize all Views (Find them once)
        val etName = findViewById<EditText>(R.id.etEditVendorName)
        val etDesc = findViewById<EditText>(R.id.etEditVendorDesc)
        val etPrice = findViewById<EditText>(R.id.etEditVendorPrice)
        val etLoc = findViewById<EditText>(R.id.etEditVendorLoc)
        val etImage = findViewById<EditText>(R.id.etEditVendorImage)
        val etSocial = findViewById<EditText>(R.id.etEditVendorSocial)
        val btnSave = findViewById<Button>(R.id.btnSaveVendorDetails)

        // 2. Catch the data from the Intent (The "Teleporter")
        val vendorId = intent.getIntExtra("VENDOR_ID", -1)
        val currentName = intent.getStringExtra("VENDOR_NAME") ?: ""
        val currentPrice = intent.getDoubleExtra("VENDOR_PRICE", 0.0)
        val currentLoc = intent.getStringExtra("VENDOR_LOC") ?: ""
        val currentDesc = intent.getStringExtra("VENDOR_DESC") ?: ""
        val currentImage = intent.getStringExtra("VENDOR_IMAGE") ?: ""
        val currentSocial = intent.getStringExtra("VENDOR_SOCIAL") ?: ""

        // 3. Paint the data into the boxes so they aren't empty
        etName.setText(currentName)
        etPrice.setText(currentPrice.toString())
        etLoc.setText(currentLoc)
        etDesc.setText(currentDesc)
        etImage.setText(currentImage)
        etSocial.setText(currentSocial)

        // 4. The Save Button Logic
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newDesc = etDesc.text.toString().trim()
            val newPriceStr = etPrice.text.toString().trim()
            val newLoc = etLoc.text.toString().trim()
            val rawImage = etImage.text.toString().trim()
            val rawSocial = etSocial.text.toString().trim()

            // Handle optional links: if empty, send null to Python
            val newImage = if (rawImage.isNotEmpty()) rawImage else null
            val newSocial = if (rawSocial.isNotEmpty()) rawSocial else null

            // Validation: Ensure essential fields aren't empty
            if (newName.isEmpty() || newPriceStr.isEmpty() || newLoc.isEmpty()) {
                Toast.makeText(this, "Please fill out name, price, and location!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPrice = newPriceStr.toDoubleOrNull() ?: 0.0

            // Pack the data
            val updateRequest = VendorUpdateRequest(newName, newDesc, newPrice, newLoc, newImage, newSocial)

            // Fire the PUT request to Python
            if (vendorId != -1) {
                RetrofitClient.instance.updateVendorDetails(vendorId, updateRequest).enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EditVendorActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@EditVendorActivity, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Toast.makeText(this@EditVendorActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}