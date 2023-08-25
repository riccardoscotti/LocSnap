package com.example.locsnap

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


class PickMediaActivity : AppCompatActivity() {
    private lateinit var pickSingleMediaShared: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var shared_by: String
    private lateinit var receiver: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras != null) {
            shared_by = intent.extras!!.get("shared_by") as String
            receiver = intent.extras!!.get("receiver") as String

            Log.d("friends", "received ${shared_by} and ${receiver}")
        }

        pickSingleMediaShared =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    Log.d("friends", "Starting...")
                    Log.d("exif", "Uri.path: ${uri.path}")
                    val isr = contentResolver.openInputStream(uri)!!.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(isr, 0, isr.size)
                    UploadUtils.uploadImage(bitmap, receiver, shared_by, this)
                    Log.d("friends", "Started successfully.")

                    this.finish()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        pickSingleMediaShared.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
