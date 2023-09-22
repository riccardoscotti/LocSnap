package com.example.locsnap.utils

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.FragmentUtils
import com.example.locsnap.R
import com.example.locsnap.UploadUtils
import com.example.locsnap.fragments.ChooseFragment

class SingleCollectionInListAdapter(
    private val collection_names: Array<String>,
    private val fragment: ChooseFragment
    ) : RecyclerView.Adapter<SingleCollectionInListAdapter.MyViewHolder>() {

    class MyViewHolder(val ll: LinearLayout) : RecyclerView.ViewHolder(ll)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.collection_in_list, parent, false) as LinearLayout
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val collName = holder.ll.findViewById<TextView>(R.id.singleCollectionName)
        collName.text = collection_names.get(position)
        collName.setTextColor(Color.parseColor("#FFFFFF"))

        val shareView = holder.ll.findViewById<ImageView>(R.id.singleShare)
        shareView.setImageResource(R.drawable.share)

        val deleteView = holder.ll.findViewById<ImageView>(R.id.singleDelete)
        deleteView.setImageResource(R.drawable.delete)

        deleteView.setOnClickListener {
            UploadUtils.deleteCollection(fragment.getLoggedUser(), collection_names.get(position), fragment)
        }

        shareView.setOnClickListener {
            FragmentUtils.getFriends(fragment.getLoggedUser(), fragment, "tag", collection_names.get(position))
        }

    }
    override fun getItemCount() = collection_names.size
}