package com.example.locsnap

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

class ChooseFragment : Fragment() {

    private lateinit var input : String
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
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

        return inflater.inflate(R.layout.fragment_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)
        val uploadButton = view.findViewById<Button>(R.id.uploadButton)

        welcomeText.text = input

        uploadButton.setOnClickListener {
            // Launch the photo picker and let the user choose only images.
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}