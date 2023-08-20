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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChooseFragment : Fragment() {

    private lateinit var loggedUser : String
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var pickSingleMedia: ActivityResultLauncher<PickVisualMediaRequest>
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
                FileManagerUtils.saveNewCollection(this, uris)
//                Log.d("localStorage", "You now have " +
//                        "${this.requireContext().getExternalFilesDir(null)?.listFiles()?.size} " +
//                        "elements.")
            }
        }

        pickSingleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val bytes = requireActivity().contentResolver.openInputStream(uri)?.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                    UploadUtils.uploadImage(bitmap, this)
                }
            }

        return inflater.inflate(R.layout.fragment_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)
        val uploadButton = view.findViewById<Button>(R.id.uploadButton)
        val plusIcon = view.findViewById<ImageView>(R.id.plusIcon)
        val camIcon = view.findViewById<ImageView>(R.id.camIcon)
        val picIcon = view.findViewById<ImageView>(R.id.picIcon)
        val shareButton = view.findViewById<ImageView>(R.id.shareButton)

        welcomeText.text = "${welcomeText.text} $loggedUser!"

        plusIcon.setOnClickListener {

            var elementsToAnimate = HashMap<View, Float>()
            elementsToAnimate.put(camIcon, -250f)
            elementsToAnimate.put(picIcon, -500f)

            buttonsUp = FragmentUtils.animateElements(elementsToAnimate, buttonsUp)

            camIcon.setOnClickListener {
                startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 111)
            }

            picIcon.setOnClickListener {
                // Launch the photo picker allowing the user to select more images.
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

        shareButton.setOnClickListener {
            // Opens dialog with user's friends
//            FragmentUtils.showFriends(loggedUser, this).create().show()
            FragmentUtils.getFriends(loggedUser, this)
        }

        uploadButton.setOnClickListener {

            // User needs to choose to upload either a single image or an already-created collection.
            AlertDialog.Builder(requireContext())
                .setTitle("Choose an option")
                .setItems(arrayOf("Single photo", "Existing collection"),
                    { dialog, which ->
                        when(which) {
                            0 -> { // Single photo
                                // Launch the photo picker allowing user to select only one image.
                                pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }

                            1 -> { // Existing collection
                                FileManagerUtils.showExistingCollections(this)
                            }
                        }
                    }).create().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Retrieves captured photo
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data!!.getExtras()!!.get("data") as Bitmap
            UploadUtils.uploadImage(capturedImage, this)
        }
    }
}