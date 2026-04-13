package com.example.eventplan // KEEP YOUR PACKAGE NAME

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClientBookingAdapter(
    private val bookings: List<ClientBooking>,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<ClientBookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // We are using Android's built-in 2-line list item
        val tvVendorAndDate: TextView = view.findViewById(android.R.id.text1)
        val tvLocation: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.tvVendorAndDate.text = "${booking.vendorName} on ${booking.bookedDate}"
        holder.tvLocation.text = "Location: ${booking.location}"

        // THE NEW LOGIC: When they press and hold the item, trigger the delete!
        holder.itemView.setOnLongClickListener {
            onLongClick(booking.bookingId)
            true // Tells Android we successfully handled the long click
        }
    }

    override fun getItemCount() = bookings.size
}