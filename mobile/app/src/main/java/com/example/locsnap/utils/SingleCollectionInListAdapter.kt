package com.example.locsnap.utils

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.R

class SingleCollectionInListAdapter(private val collection_names: MutableList<String>) :
    RecyclerView.Adapter<SingleCollectionInListAdapter.MyViewHolder>() {

    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.collection_in_list, parent, false)
        val textView = view.findViewById<TextView>(R.id.singleCollectionName)
        return MyViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = "${collection_names[position]}"
    }

    override fun getItemCount() = collection_names.size
}