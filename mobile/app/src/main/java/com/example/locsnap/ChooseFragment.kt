package com.example.locsnap

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.io.File

class ChooseFragment : Fragment() {

    private lateinit var loggedUser : String
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var selected_collection: File
    private var maxPhotos : Int = 10
    private var buttonsUp : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args = this.arguments
        loggedUser = args?.getString("loggedUsername").toString()

        pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxPhotos)) { uris ->
                if (uris.isNotEmpty()) {
//                    FileManagerUtils.createNewCollection(this, uris)
                }
            }

        return inflater.inflate(R.layout.fragment_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)
        val uploadButton = view.findViewById<Button>(R.id.uploadButton)
        val camIcon = view.findViewById<ImageView>(R.id.camIcon)
        val plusIcon = view.findViewById<ImageView>(R.id.plusIcon)
        val shareButton = view.findViewById<ImageView>(R.id.shareButton)

        welcomeText.text = "${welcomeText.text} $loggedUser!"

        camIcon.setOnClickListener {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 111)
        }

        // Opens dialog with user's friends
        shareButton.setOnClickListener {
            FragmentUtils.getFriends(loggedUser, this)
        }

        // Uploads a collection
        uploadButton.setOnClickListener {
            FileManagerUtils.showExistingCollections(this)
        }

        plusIcon.setOnClickListener {
            val storedCollections = requireContext().getExternalFilesDir(null)?.listFiles()
            val fileNames = mutableListOf<String>()
            val filePaths = mutableListOf<String>()
            fileNames.add("Create new collection")
            filePaths.add("/")
            storedCollections?.forEach { file ->
                fileNames.add(file.name)
                filePaths.add(file.absolutePath)
            }

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select the collection you want to upload")
                .setItems(fileNames.toTypedArray()
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 222)
                        }
                        else -> {
                            selected_collection = File(filePaths[which])
                            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 333)
                        }
                    }
                }

            builder.create().show()
        }
    }

    /*
    * 111 -> Requested capture from camIcon
    * 222 -> Requested capture for the creation of a new collection
    * 333 -> Requested capture to be added to an already existing collection
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Retrieves captured photo
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data?.getExtras()!!.get("data") as Bitmap
            UploadUtils.uploadImage(capturedImage, loggedUser, "/", this.requireActivity())
        } else if (requestCode == 222 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data?.getExtras()!!.get("data") as Bitmap
            FileManagerUtils.createNewCollection(capturedImage, this)
        } else if(requestCode == 333 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data?.getExtras()!!.get("data") as Bitmap
            FileManagerUtils.addToCollection(selected_collection, capturedImage, this)
        }
    }
}