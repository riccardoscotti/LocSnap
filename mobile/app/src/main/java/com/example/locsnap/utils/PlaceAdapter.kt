package com.example.locsnap.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.R

class PlaceAdapter(private val places: Array<String>) :
    RecyclerView.Adapter<PlaceAdapter.MyViewHolder>() {

    class MyViewHolder(val place: TextView) : RecyclerView.ViewHolder(place)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val placeView = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_place, parent, false) as TextView
        return MyViewHolder(placeView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.place.text = places[position]
    }

    override fun getItemCount() = places.size
}