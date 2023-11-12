package com.example.locsnap.utils

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.R

class ImagesAdapter(private val images: MutableList<Bitmap>, private val imagesNames: Array<String>) :
    RecyclerView.Adapter<ImagesAdapter.MyViewHolder>() {

    class MyViewHolder(val cl: ConstraintLayout) : RecyclerView.ViewHolder(cl)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val cl = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_image_received, parent, false) as ConstraintLayout

        return MyViewHolder(cl)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.cl.findViewById<ImageView>(R.id.single_image_view).setImageBitmap(images[position])
        holder.cl.findViewById<TextView>(R.id.single_image_name).setText(imagesNames[position])
    }

    override fun getItemCount() = images.size
}