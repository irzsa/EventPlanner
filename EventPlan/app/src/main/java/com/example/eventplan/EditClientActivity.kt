package com.example.eventplan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditClientActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_client)

        val etName = findViewById<EditText>(R.id.etEditClientName)
        val etPhone = findViewById<EditText>(R.id.etEditClientPhone)
        val btnSave = findViewById<Button>(R.id.btnSaveClientDetails)

        // Open the vault to get their User ID!
        val sharedPreferences = getSharedPreferences("EventPlanPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        // ==========================================
        // PHASE 1: THE LOADING PHASE (Runs instantly)
        // ==========================================
        if (userId != -1) {
            RetrofitClient.instance.getUserProfile(userId).enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        // Instantly type the old data into the boxes!
                        etName.setText(response.body()!!.fullName)
                        etPhone.setText(response.body()!!.phoneNumber)
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    // Fail silently, boxes just stay blank
                }
            })
        }

        // ==========================================
        // PHASE 2: THE SAVING PHASE (Runs on click)
        // ==========================================
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()

            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(this, "Please fill out both fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pack the new data into our Pydantic format
            val updateRequest = UserUpdateRequest(newName, newPhone)

            if (userId != -1) {
                // Notice we use updateUserProfile here, NOT getUserProfile!
                RetrofitClient.instance.updateUserProfile(userId, updateRequest).enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EditClientActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            finish() // Closes the screen and drops them back at the Client Hub
                        } else {
                            Toast.makeText(this@EditClientActivity, "Error saving profile.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Toast.makeText(this@EditClientActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}