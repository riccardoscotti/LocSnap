package com.example.locsnap.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.*
import com.example.locsnap.utils.ImagesAdapter
import com.example.locsnap.utils.SingleCollectionInListAdapter
import java.io.File


class ChooseFragment : Fragment() {

    private lateinit var loggedUser : String
    private lateinit var selected_collection: File
    private var last_known_location : Location? = null
    private lateinit var imageView: ImageView

    fun getLoggedUser() : String {
        return this.loggedUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args = this.arguments
        loggedUser = args?.getString("loggedUsername").toString()

        return inflater.inflate(R.layout.fragment_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)
        val uploadButton = view.findViewById<Button>(R.id.uploadButton)
        val camIcon = view.findViewById<ImageView>(R.id.camIcon)
        val plusIcon = view.findViewById<ImageView>(R.id.plusIcon)
        val shareButton = view.findViewById<ImageView>(R.id.shareButton)
        val addUserIcon = view.findViewById<ImageView>(R.id.addFriendIcon)
        val nearbyButton = view.findViewById<Button>(R.id.nearbyButton)

        val names = mutableListOf<String>()
        names.add("Collezione1.bin")
        names.add("Collezione2.bin")
        names.add("Collezione3.bin")

        val recyclerView = view.findViewById<RecyclerView>(R.id.listPhotosRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.adapter = SingleCollectionInListAdapter(names)

        welcomeText.text = "${welcomeText.text} $loggedUser!"

        camIcon.setOnClickListener {
            this.openCamera()
        }

        // Opens dialog with user's friends
        shareButton.setOnClickListener {
            FragmentUtils.getFriends(loggedUser, this)
        }

        // Opens dialog with user's friends
        nearbyButton.setOnClickListener {
            val intent = Intent(requireContext(), getLocationActivity::class.java)
            startActivityForResult(intent, 777)
        }

        // Uploads a collection
        uploadButton.setOnClickListener {
            FileManagerUtils.showExistingCollections(this, "upload")
        }

        addUserIcon.setOnClickListener {
            val dialog = Dialog(this.requireContext())
            dialog.setContentView(R.layout.add_friend_dialog)
            val proceed = dialog.findViewById<Button>(R.id.confirmButton)

            proceed.setOnClickListener {
                val friendText = dialog.findViewById<EditText>(R.id.friendText)
                FragmentUtils.addFriend(this.loggedUser, friendText.text.toString(), this)
                dialog.dismiss()
            }

            dialog.show()
        }

        plusIcon.setOnClickListener {
            val collections = mutableListOf<String>()
            val filepaths = FileManagerUtils.getCollections().keys
            collections.add("Create new collection")

            for(key in filepaths)
                collections.add(File(key).name)

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select the collection you are interested in")
                .setItems(collections.toTypedArray()
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(requireContext(), getLocationActivity::class.java)
                            startActivityForResult(intent, 444)
                        }
                        else -> {
                            this.selected_collection = File(filepaths.toTypedArray().get(which-1))
                            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 333)
                        }
                    }
                }.create().show()
        }
    }

    fun openCamera(taggedFriend: String = "") {
        val intent = Intent(requireContext(), getLocationActivity::class.java)

        if (taggedFriend != "") {
            intent.putExtra("tagged_friend", taggedFriend)
        }
        startActivityForResult(intent, 555)
    }

    fun getLastKnownLocation() : Location? {
        return this.last_known_location
    }

    /*
    * 111 -> Requested capture from camIcon
    * 222 -> Requested capture for the creation of a new collection
    * 333 -> Requested capture to be added to an already existing collection
    * 444 -> Retrieves GPS location and creates new collection
    * 555 -> Retrieves GPS location and upload single photo
    * */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Retrieves captured photo
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data?.extras!!["data"] as Bitmap
            val taggedFriend = data.extras!!.get("tagged_friend") as String?
            if (taggedFriend != null) {
                UploadUtils.uploadImage(capturedImage, loggedUser, this, taggedFriend.toString())
            }
            UploadUtils.uploadImage(capturedImage, loggedUser, this)
        } else if (requestCode == 222 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data?.extras!!["data"] as Bitmap
            FileManagerUtils.createNewCollection(capturedImage, this, this.last_known_location)
        } else if(requestCode == 333 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data?.extras!!["data"] as Bitmap
            FileManagerUtils.addImageToCollection(this.selected_collection, capturedImage, this)
        } else if(requestCode == 444 && resultCode == Activity.RESULT_OK) {
            this.last_known_location = data?.extras!!.get("gps_location") as Location
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 222)
        } else if(requestCode == 555 && resultCode == Activity.RESULT_OK) {
            this.last_known_location = data?.extras!!.get("gps_location") as Location
            val taggedFriend = data.extras!!.get("tagged_friend") as String?
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (taggedFriend != null)
                intent.putExtra("tagged_friend", taggedFriend)

            startActivityForResult(intent, 111)
        } else if(requestCode == 777 && resultCode == Activity.RESULT_OK) {
            this.last_known_location = data?.extras!!.get("gps_location") as Location
            UploadUtils.showNearestPhotos(3, this.last_known_location!!, this)
        }
    }
}