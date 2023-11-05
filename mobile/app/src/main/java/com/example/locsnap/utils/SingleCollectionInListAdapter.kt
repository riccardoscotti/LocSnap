package com.example.locsnap.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.R
import com.example.locsnap.fragments.ChooseFragment
import kotlinx.coroutines.*
import org.json.JSONObject

class SingleCollectionInListAdapter(
    private val collectionNames: Array<String>,
    private val fragment: ChooseFragment
    ) : RecyclerView.Adapter<SingleCollectionInListAdapter.MyViewHolder>() {

        private var imagesList = JSONObject() // Name of images in a collection
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
        collName.text = collectionNames[position]
        collName.setTextColor(Color.parseColor("#FFFFFF"))

        val addView = holder.ll.findViewById<ImageView>(R.id.addIcon)
        addView.setImageResource(R.drawable.plus)

        val deleteView = holder.ll.findViewById<ImageView>(R.id.deleteIcon)
        deleteView.setImageResource(R.drawable.delete)

        collName.setOnClickListener {

            fragment.setSelectedCollection(collName.text.toString())

            GlobalScope.async {
                UploadUtils.imagesOfCollection(fragment, thisInstance, collectionNames[position])

                withContext(Dispatchers.Main) {
                    val dialog = Dialog(fragment.requireContext())
                    dialog.setContentView(R.layout.list_images_dialog)
                    dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                    val recyclerView = dialog.findViewById<RecyclerView>(R.id.images_recycler)
                    recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())
                    recyclerView.adapter = ImagesListAdapter(thisInstance.imagesList, fragment)
                    recyclerView.addItemDecoration(DividerItemDecoration(fragment.requireContext(), LinearLayoutManager.VERTICAL))
                    recyclerView.addItemDecoration(VerticalSpaceItemDecoration(35))

                    dialog.show()
                }
            }
        }

        addView.setOnClickListener {
            fragment.setSelectedCollection(this.collectionNames[position])
            fragment.openCamera()
        }

        deleteView.setOnClickListener {
            UploadUtils.deleteCollection(collectionNames[position], fragment)
        }
    }

    fun setImagesList(imagesList: JSONObject) {
        this.imagesList = imagesList
    }

    override fun getItemCount() = collectionNames.size
}