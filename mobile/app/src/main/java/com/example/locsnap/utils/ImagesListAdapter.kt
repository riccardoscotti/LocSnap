package com.example.locsnap.utils

import android.app.Dialog
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.R
import com.example.locsnap.UploadUtils
import com.example.locsnap.fragments.ChooseFragment
import org.json.JSONObject

class ImagesListAdapter(private val images_list: JSONObject,
                        private val fragment: ChooseFragment
) :
    RecyclerView.Adapter<ImagesListAdapter.MyViewHolder>() {

    class MyViewHolder(val cl: ConstraintLayout) : RecyclerView.ViewHolder(cl)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val cl = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_image_of_collection, parent, false) as ConstraintLayout
        return MyViewHolder(cl)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val placeText = holder.cl.findViewById<TextView>(R.id.nameView)
        placeText.text = this.images_list.getJSONObject("$position").getString("name")

        val imageBytes = Base64.decode(this.images_list.getJSONObject("$position").getString("image"), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val image = holder.cl.findViewById<ImageView>(R.id.imageofcollection)
        image.setImageBitmap(bitmap)

        placeText.setOnClickListener {
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

                UploadUtils.updateImageInfo(fragment, placeText.text.toString(), publicCheck.isChecked, type)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount() = images_list.length()
}