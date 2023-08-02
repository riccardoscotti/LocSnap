package com.example.locsnap

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

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
                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    //val inputStream = requireActivity().contentResolver.openInputStream(uri)?.close()
                    // val bytes = inputStream?.readBytes()

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
                    DialogInterface.OnClickListener { dialog, which ->
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