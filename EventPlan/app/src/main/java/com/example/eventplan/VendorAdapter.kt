package com.example.eventplan

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VendorAdapter(private val vendors: List<Vendor>) : RecyclerView.Adapter<VendorAdapter.VendorViewHolder>() {

    class VendorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(android.R.id.text1)
        val tvPrice: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        // Using Android's built-in two-line list item
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return VendorViewHolder(view)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        val vendor = vendors[position]
        holder.tvName.text = vendor.name
        holder.tvPrice.text = "$${vendor.pricePerHour} / hr - ${vendor.location}"

        // THE CLICK LISTENER: Teleport to VendorDetailsActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VendorDetailsActivity::class.java).apply {
                putExtra("VENDOR_ID", vendor.id)
                putExtra("VENDOR_NAME", vendor.name)
                putExtra("VENDOR_DESC", vendor.description)
                putExtra("VENDOR_PRICE", vendor.pricePerHour)
                putExtra("VENDOR_LOC", vendor.location)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = vendors.size
}