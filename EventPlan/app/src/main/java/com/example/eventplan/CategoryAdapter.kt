package com.example.eventplan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// The adapter takes a list of Strings (our category names)
class CategoryAdapter(private val categories: List<String>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // 1. This connects to the views inside item_category.xml
    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
    }

    // 2. This tells the adapter which layout file to use for the items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    // 3. This puts the actual data (the category name) into the TextView
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.tvCategoryName.text = categories[position]
    }

    // 4. This tells the adapter how many items are in the list
    override fun getItemCount(): Int {
        return categories.size
    }
}