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
import com.example.locsnap.fragments.ChooseFragment
import org.json.JSONObject

class ImagesListAdapter(private val imagesList: JSONObject,
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
        val imageName = holder.cl.findViewById<TextView>(R.id.nameView)
        imageName.text = this.imagesList.getJSONObject("$position").getString("name")

        val imageBytes = Base64.decode(this.imagesList.getJSONObject("$position").getString("image"), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val image = holder.cl.findViewById<ImageView>(R.id.imageofcollection)
        image.setImageBitmap(bitmap)

        val shareBtn = holder.cl.findViewById<ImageView>(R.id.shareIcon2)
        shareBtn.setImageResource(R.drawable.share)

        val editBtn = holder.cl.findViewById<ImageView>(R.id.editIcon)
        editBtn.setImageResource(R.drawable.edit)

        shareBtn.setOnClickListener {
            FragmentUtils.getFriends(fragment.getLoggedUser(),
                fragment,
                "tag",
                this.imagesList.getJSONObject("$position").getString("name")
            )
        }

        editBtn.setOnClickListener {

            val dialog = Dialog(fragment.requireContext())
            dialog.setContentView(R.layout.info_upload_dialog)
            val proceed = dialog.findViewById<Button>(R.id.confirmButton)
            val publicCheck = dialog.findViewById<CheckBox>(R.id.publicCheckBox)
            val city = dialog.findViewById<RadioButton>(R.id.radio_city)
            val mountain = dialog.findViewById<RadioButton>(R.id.radio_mountain)
            val sea = dialog.findViewById<RadioButton>(R.id.radio_sea)

            val collectionName = dialog.findViewById<EditText>(R.id.collection_tv)
            collectionName.isEnabled = false
            collectionName.setText(fragment.getSelectedCollection())

            val newImageName = dialog.findViewById<EditText>(R.id.image_tv)

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

                if (newImageName.text.isBlank() || !(city.isChecked || mountain.isChecked || sea.isChecked) )
                    Toast.makeText(fragment.requireContext(), "You must fill mandatory fields.", Toast.LENGTH_SHORT).show()
                else
                    UploadUtils.updateImageInfo(
                        fragment,
                        imageName.text.toString(), // Old image name
                        newImageName.text.toString(),
                        collectionName.text.toString(),
                        publicCheck.isChecked,
                        type
                    )

                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount() = imagesList.length()
}