package com.example.eventplan

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VendorSearchAdapter(private val vendors: List<VendorDashboardResponse>) : RecyclerView.Adapter<VendorSearchAdapter.SearchViewHolder>() {

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(android.R.id.text1)
        val tvDetails: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        // Using Android's built-in 2-line list item again for speed!
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val vendor = vendors[position]
        holder.tvName.text = vendor.vendorName
        holder.tvDetails.text = "${vendor.location}  •  $${vendor.pricePerHour}/hr"

        // When they click a search result, teleport them to the Details/Booking screen!
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VendorDetailsActivity::class.java).apply {
                putExtra("VENDOR_ID", vendor.vendorId)
                putExtra("VENDOR_NAME", vendor.vendorName)
                putExtra("VENDOR_PRICE", vendor.pricePerHour)
                putExtra("VENDOR_LOC", vendor.location)
                // If description is null, just pass an empty string
                putExtra("VENDOR_DESC", "Check out my profile to book!")
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = vendors.size
}