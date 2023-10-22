package com.example.locsnap.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.provider.Settings.Global
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.FragmentUtils
import com.example.locsnap.R
import com.example.locsnap.UploadUtils
import com.example.locsnap.fragments.ChooseFragment
import kotlinx.coroutines.*
import org.json.JSONObject

class SingleCollectionInListAdapter(
    private val collection_names: Array<String>,
    private val fragment: ChooseFragment
    ) : RecyclerView.Adapter<SingleCollectionInListAdapter.MyViewHolder>() {

        private var images_list = JSONObject() // Name of images in a collection
        private var thisInstance = this
    class MyViewHolder(val ll: LinearLayout) : RecyclerView.ViewHolder(ll)

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }

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

        collName.setOnClickListener {

            GlobalScope.async {
                UploadUtils.imagesOfCollection(fragment, thisInstance, collection_names.get(position))

                withContext(Dispatchers.Main) {
                    val dialog = Dialog(fragment.requireContext())
                    dialog.setContentView(R.layout.list_images_dialog)
                    dialog.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent);
                    val recyclerView = dialog.findViewById<RecyclerView>(R.id.images_recycler)
                    recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())
                    recyclerView.adapter = ImagesListAdapter(thisInstance.images_list, fragment)
                    recyclerView.addItemDecoration(DividerItemDecoration(fragment.requireContext(), LinearLayoutManager.VERTICAL))
                    recyclerView.addItemDecoration(VerticalSpaceItemDecoration(35))

                    dialog.show()
                }
            }
        }

        deleteView.setOnClickListener {
            UploadUtils.deleteCollection(fragment.getLoggedUser(), collection_names.get(position), fragment)
        }

        shareView.setOnClickListener {
            FragmentUtils.getFriends(fragment.getLoggedUser(), fragment, "tag", collection_names.get(position))
        }
    }

    fun setImagesList(images_list: JSONObject) {
        this.images_list = images_list
    }

    override fun getItemCount() = collection_names.size
}