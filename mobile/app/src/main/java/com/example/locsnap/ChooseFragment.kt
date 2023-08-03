package com.example.locsnap

import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import java.io.FileOutputStream
import java.io.IOException
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import java.io.File

class ChooseFragment : Fragment() {

    private lateinit var input : String
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var pickSingleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var maxPhotos : Int = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args = this.arguments
        input = args?.getString("loggedUsername").toString()

        pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxPhotos)) { uris ->
            if (uris.isNotEmpty()) {
                val file = File(this
                    .requireContext()
                    .getExternalFilesDir(null),
                    "collection_${this.requireContext().getExternalFilesDir(null)?.listFiles()?.size}.bin")

                val urisArray = mutableListOf<Uri>()
                for (uri in uris) {
                    urisArray.add(uri)
                }

                try {
                    val outputStream = FileOutputStream(file)
                    for (uri in uris) {
                        val uriBytes = requireActivity().contentResolver.openInputStream(uri)?.readBytes()
                        outputStream.write(uriBytes)
                    }

                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                Log.d("localStorage", "You now have ${this.requireContext().getExternalFilesDir(null)?.listFiles()?.size} elements.")

            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    val bytes = requireActivity().contentResolver.openInputStream(uri)?.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                    UploadUtils.uploadImage(bitmap, this)

                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }
        return inflater.inflate(R.layout.fragment_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)
        val uploadButton = view.findViewById<Button>(R.id.uploadButton)
        val newCollectionButton = view.findViewById<Button>(R.id.collectionButton)

        welcomeText.text = "${welcomeText.text} $input!"

        uploadButton.setOnClickListener {

            // User needs to choose to upload either a single image or an already-created collection.
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose an option")
                .setItems(arrayOf("Single photo", "Existing collection"),
                    { dialog, which ->
                        when(which) {
                            0 -> { // Single photo
                                // Launch the photo picker allowing user to select only one image.
                                pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }

                            1 -> { // Existing collection
                                // How to retrieve existing collections?
                            }
                        }
                    })

            builder.create().show()
        }

        newCollectionButton.setOnClickListener {
            // Launch the photo picker allowing the user to select more images.
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


    }
}