package com.example.locsnap

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.locsnap.databinding.FragmentFirstBinding
import com.google.android.material.switchmaterial.SwitchMaterial

class UploadImagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val uploadButton = view.findViewById<Button>(R.id.uploadButton)

        val chooseFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val inputStream = uri?.let { activity?.contentResolver?.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            //uploadImage(bitmap, queue)
        }

        //uploadButton.setOnClickListener {
        //    chooseFile.launch("image/*") // Open file chooser
        //}
    }
}