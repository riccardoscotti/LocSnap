package com.example.locsnap.utils

import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.R
import com.example.locsnap.UploadUtils
import com.example.locsnap.fragments.ChooseFragment

class ImagesListAdapter(private val images_list: Array<String>,
                        private val fragment: ChooseFragment
) :
    RecyclerView.Adapter<ImagesListAdapter.MyViewHolder>() {

    class MyViewHolder(val place: TextView) : RecyclerView.ViewHolder(place)



    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val placeView = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_place, parent, false) as TextView
        return MyViewHolder(placeView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.place.text = images_list[position]

        holder.place.setOnClickListener {
            val dialog = Dialog(fragment.requireContext())
            dialog.setContentView(R.layout.info_upload_dialog)
            val proceed = dialog.findViewById<Button>(R.id.confirmButton)
            val publicCheck = dialog.findViewById<CheckBox>(R.id.publicCheckBox)
            val city = dialog.findViewById<RadioButton>(R.id.radio_city)
            val mountain = dialog.findViewById<RadioButton>(R.id.radio_mountain)
            val sea = dialog.findViewById<RadioButton>(R.id.radio_sea)

            proceed.setOnClickListener {
                var type = ""

                fun setType(selectedType: String) {
                    type = selectedType
                }

                if (city.isChecked)
                    setType(city.text.toString())

                else if (mountain.isChecked)
                    setType(mountain.text.toString())

                else if (sea.isChecked)
                    setType(sea.text.toString())

                UploadUtils.updateImageInfo(fragment, holder.place.text.toString(), publicCheck.isChecked, type)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount() = images_list.size
}