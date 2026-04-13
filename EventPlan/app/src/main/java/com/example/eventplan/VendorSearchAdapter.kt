package com.example.eventplan

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VendorSearchAdapter(private val vendors: List<VendorDashboardResponse>) : RecyclerView.Adapter<VendorSearchAdapter.SearchViewHolder>() {

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // We match these IDs to the ones we just made in item_vendor_card.xml!
        val tvName: TextView = view.findViewById(R.id.tvVendorCardName)
        val tvDetails: TextView = view.findViewById(R.id.tvVendorCardDetails)
        val ivPic: ImageView = view.findViewById(R.id.ivVendorPic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        // Here we inflate our brand new card layout!
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vendor_card, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val vendor = vendors[position]
        holder.tvName.text = vendor.vendorName
        holder.tvDetails.text = "${vendor.location}  •  $${vendor.pricePerHour}/hr"

        // PRINT THE URL TO THE CONSOLE!
        println("🚨🚨🚨 DEBUG: The image URL for ${vendor.vendorName} is: ${vendor.imageUrl}")

        // Let Glide magically load the image URL into the circle!
        if (!vendor.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(vendor.imageUrl)
                .centerCrop()
                .error(android.R.drawable.ic_dialog_alert)
                .into(holder.ivPic)
        } else {
            // If they have no picture yet, just clear it so it shows the grey background
            holder.ivPic.setImageDrawable(null)
        }

        // The Click Listener (Same as before)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VendorDetailsActivity::class.java).apply {
                putExtra("VENDOR_ID", vendor.vendorId)
                putExtra("VENDOR_NAME", vendor.vendorName)
                putExtra("VENDOR_PRICE", vendor.pricePerHour)
                putExtra("VENDOR_LOC", vendor.location)
                putExtra("VENDOR_DESC", "Check out my profile to book!")
                putExtra("VENDOR_IMAGE", vendor.imageUrl)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = vendors.size
}