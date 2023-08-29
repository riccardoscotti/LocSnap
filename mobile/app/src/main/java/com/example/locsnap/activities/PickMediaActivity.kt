package com.example.locsnap

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PickMediaActivity : AppCompatActivity() {
    private lateinit var pickSingleMediaShared: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var shared_by: String
    private lateinit var receiver: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras != null) {
            shared_by = intent.extras!!.get("shared_by") as String
            receiver = intent.extras!!.get("receiver") as String
        }
    }

    override fun onStart() {
        super.onStart()
        pickSingleMediaShared.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
