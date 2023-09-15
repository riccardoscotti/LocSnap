package com.example.locsnap.utils

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.R

class SingleCollectionInListAdapter(private val collection_names: MutableList<String>) :
    RecyclerView.Adapter<SingleCollectionInListAdapter.MyViewHolder>() {

    class MyViewHolder(val ll: LinearLayout) : RecyclerView.ViewHolder(ll)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.collection_in_list, parent, false) as LinearLayout
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val collName = holder.ll.findViewById<TextView>(R.id.singleCollectionName)
        collName.text = "${collection_names.get(position)}"

        val shareView = holder.ll.findViewById<ImageView>(R.id.singleShare)
        shareView.setImageResource(R.drawable.share)
    }

    override fun getItemCount() = collection_names.size
}