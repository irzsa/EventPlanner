package com.example.eventplan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeaturedVendorAdapter(private val vendors: List<VendorDashboardResponse>) :
    RecyclerView.Adapter<FeaturedVendorAdapter.FeaturedViewHolder>() {

    // 1. The ViewHolder finds the text boxes inside our item_featured_vendor.xml
    inner class FeaturedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvFeaturedName)
        val tvDetails: TextView = itemView.findViewById(R.id.tvFeaturedDetails)
    }

    // 2. This tells the adapter which layout file to use for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_vendor, parent, false)
        return FeaturedViewHolder(view)
    }

    // 3. This is where we "paint" the data onto the card
    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        val vendor = vendors[position]

        holder.tvName.text = vendor.vendorName
        // Format the details to look clean: "Downtown • $100.0/hr"
        holder.tvDetails.text = "${vendor.location}  •  $${vendor.pricePerHour}/hr"
    }

    // 4. Tells the adapter how many items we have total
    override fun getItemCount(): Int {
        return vendors.size
    }
}