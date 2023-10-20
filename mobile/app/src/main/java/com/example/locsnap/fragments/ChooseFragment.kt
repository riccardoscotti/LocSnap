package com.example.locsnap.fragments

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.*
import com.example.locsnap.activities.getLocationService
import com.example.locsnap.utils.SingleCollectionInListAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class ChooseFragment : Fragment() {
    private lateinit var loggedUser : String
    private lateinit var selected_collection: File
    private var retrievedCollections = mutableListOf<String>()
    private var last_known_location : Location? = null
    private lateinit var recyclerView: RecyclerView
    private val thisInstance = this
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            last_known_location = intent.extras!!.get("location") as Location

            if (intent.extras!!.getString("action").equals("nearby"))
                UploadUtils.showNearestPhotos(4, last_known_location!!, thisInstance)

            else if (intent.extras!!.getString("action").equals("camera"))
                startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 222)

            else if (intent.extras!!.getString("action").equals("upload")) // If upload service was successful, refresh the fragment
                thisInstance.refresh()
        }
    }
    fun setCollections(retrievedCollections: MutableList<String>) {
        this.retrievedCollections = retrievedCollections

        recyclerView = requireView().findViewById<RecyclerView>(R.id.listPhotosRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.adapter = SingleCollectionInListAdapter(this.retrievedCollections.toTypedArray(), this)
    }

    fun getLoggedUser() : String {
        return this.loggedUser
    }

    fun refresh() {
        this.requireActivity().getSupportFragmentManager().beginTransaction().detach(this).commitNow()
        this.requireActivity().getSupportFragmentManager().beginTransaction().attach(this).commitNow()
        UploadUtils.reloadUpdatedCollections(this.loggedUser, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = this.arguments
        loggedUser = args?.getString("loggedUsername").toString()

        UploadUtils.reloadUpdatedCollections(loggedUser, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialUserLetter = view.findViewById<TextView>(R.id.initialUserLetter)
        val uploadButton = view.findViewById<Button>(R.id.uploadButton)
        val camIcon = view.findViewById<ImageView>(R.id.camIcon)
        val plusIcon = view.findViewById<ImageView>(R.id.plusIcon)
        val addUserIcon = view.findViewById<ImageView>(R.id.addFriendIcon)
        val nearbyButton = view.findViewById<Button>(R.id.nearbyButton)

        initialUserLetter.text = this.loggedUser.get(0).uppercase()

        camIcon.setOnClickListener {
            this.openCamera()
        }

        // Opens dialog with user's friends
        nearbyButton.setOnClickListener {
            val getloc = Intent(this.requireActivity(), getLocationService::class.java)
            getloc.putExtra("action", "nearby")
            this.requireActivity().startService(getloc)
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
                            this.openCamera()
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
        val cameraIntent = Intent(this.requireActivity(), getLocationService::class.java)
        cameraIntent.putExtra("action", "camera")

        if (taggedFriend != "") {
            cameraIntent.putExtra("tagged_friend", taggedFriend)
        }

        this.requireActivity().startService(cameraIntent)
    }

    fun getLastKnownLocation() : Location? {
        return this.last_known_location
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(receiver, IntentFilter("locationFilter"))
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
//            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 222)
        } else if(requestCode == 555 && resultCode == Activity.RESULT_OK) {
            this.last_known_location = data?.extras!!.get("gps_location") as Location
            val taggedFriend = data.extras!!.get("tagged_friend") as String?
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (taggedFriend != null)
                intent.putExtra("tagged_friend", taggedFriend)
            startActivityForResult(intent, 111)
        }
    }
}
