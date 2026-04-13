package com.example.eventplan

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VendorListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_list)

        // Catch the baton!
        val categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        val categoryName = intent.getStringExtra("CATEGORY_NAME")

        findViewById<TextView>(R.id.tvVendorListTitle).text = "$categoryName"
        val rvVendors = findViewById<RecyclerView>(R.id.rvVendors)
        rvVendors.layoutManager = LinearLayoutManager(this)

        if (categoryId != -1) {
            RetrofitClient.instance.getVendorsForCategory(categoryId).enqueue(object : Callback<List<Vendor>> {
                override fun onResponse(call: Call<List<Vendor>>, response: Response<List<Vendor>>) {
                    if (response.isSuccessful && response.body() != null) {
                        // Pass the vendors to the next adapter
                        rvVendors.adapter = VendorAdapter(response.body()!!)
                    }
                }
                override fun onFailure(call: Call<List<Vendor>>, t: Throwable) {
                    Toast.makeText(this@VendorListActivity, "Failed: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}