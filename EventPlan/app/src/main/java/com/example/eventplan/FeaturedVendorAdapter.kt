package com.example.eventplan

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // 🚨 Don't forget this import!

class FeaturedVendorAdapter(private val vendors: List<VendorDashboardResponse>) :
    RecyclerView.Adapter<FeaturedVendorAdapter.FeaturedViewHolder>() {

    inner class FeaturedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvFeaturedName)
        val tvDetails: TextView = itemView.findViewById(R.id.tvFeaturedDetails)
        // 1. Find the new ImageView we just created!
        val ivImage: ImageView = itemView.findViewById(R.id.ivFeaturedImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_vendor, parent, false)
        return FeaturedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        val vendor = vendors[position]

        holder.tvName.text = vendor.vendorName
        holder.tvDetails.text = "${vendor.location}  •  $${vendor.pricePerHour}/hr"

        // 2. The Glide Magic!
        // We check if the imageUrl is not null or empty before trying to load it
        if (!vendor.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(vendor.imageUrl)
                .into(holder.ivImage)
        }

        // 🚨 THE FULLY PACKED CLICK LISTENER 🚨
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VendorDetailsActivity::class.java)

            // Pack EVERYTHING into the Intent suitcase!
            intent.putExtra("VENDOR_ID", vendor.vendorId)
            intent.putExtra("VENDOR_NAME", vendor.vendorName)

            // These names must match EXACTLY what VendorDetailsActivity is looking for:
            intent.putExtra("VENDOR_LOC", vendor.location)
            intent.putExtra("VENDOR_PRICE", vendor.pricePerHour)
            intent.putExtra("VENDOR_DESC", vendor.description)
            intent.putExtra("VENDOR_IMAGE", vendor.imageUrl)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return vendors.size
    }
}