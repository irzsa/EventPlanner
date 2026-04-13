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

        val etName = findViewById<EditText>(R.id.etEditVendorName)
        val etDesc = findViewById<EditText>(R.id.etEditVendorDesc)
        val etPrice = findViewById<EditText>(R.id.etEditVendorPrice)
        val etLoc = findViewById<EditText>(R.id.etEditVendorLoc)
        val btnSave = findViewById<Button>(R.id.btnSaveVendorDetails)

        // 1. Catch the current details passed from the Hub
        val vendorId = intent.getIntExtra("VENDOR_ID", -1)
        etName.setText(intent.getStringExtra("VENDOR_NAME"))
        etPrice.setText(intent.getDoubleExtra("VENDOR_PRICE", 0.0).toString())
        etLoc.setText(intent.getStringExtra("VENDOR_LOC"))
        // Note: We didn't fetch the description in the Dashboard endpoint earlier,
        // so we leave it blank for them to fill out fresh!

        // 2. The Save Button Logic
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newDesc = etDesc.text.toString().trim()
            val newPriceStr = etPrice.text.toString().trim()
            val newLoc = etLoc.text.toString().trim()

            if (newName.isEmpty() || newPriceStr.isEmpty() || newLoc.isEmpty()) {
                Toast.makeText(this, "Please fill out the essential fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPrice = newPriceStr.toDoubleOrNull() ?: 0.0

            // Pack the data into the Pydantic Bouncer format
            val updateRequest = VendorUpdateRequest(newName, newDesc, newPrice, newLoc)

            // Fire the PUT request to Python
            if (vendorId != -1) {
                RetrofitClient.instance.updateVendorDetails(vendorId, updateRequest).enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EditVendorActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            finish() // Closes this screen and goes back to the Hub
                        } else {
                            Toast.makeText(this@EditVendorActivity, "Failed to update.", Toast.LENGTH_SHORT).show()
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