package com.example.locsnap.fragments

import android.animation.ObjectAnimator
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.*
import com.example.locsnap.activities.GetLocationService
import com.example.locsnap.activities.RecommendActivity
import com.example.locsnap.utils.FragmentUtils
import com.example.locsnap.utils.SingleCollectionInListAdapter
import com.example.locsnap.utils.UploadUtils
import org.json.JSONArray
import org.json.JSONObject

class ChooseFragment : Fragment() {
    private lateinit var loggedUser : String
    private var selectedCollection: String = ""
    private var lastKnownLocation : Location? = null
    private var animated = false
    private lateinit var recyclerView: RecyclerView
    private val thisInstance = this
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            thisInstance.lastKnownLocation = intent.extras!!.get("location") as Location

            // Show a dialog to allow user to insert num_photos value
            if (intent.extras!!.getString("action").equals("nearby")) {
                val dialog = Dialog(thisInstance.requireActivity())
                dialog.setContentView(R.layout.nearby_dialog)
                val proceed = dialog.findViewById<Button>(R.id.confirmButton)
                val numPhotosTV = dialog.findViewById<TextView>(R.id.numPhotosTV)

                proceed.setOnClickListener {
                    UploadUtils.showNearestPhotos(numPhotosTV.text.toString().toInt(), thisInstance.lastKnownLocation!!, thisInstance)
                }
                dialog.show()

            } else if (intent.extras!!.getString("action").equals("camera")) {
                startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 111)
            }
        }
    }
    fun setCollections(retrievedCollections: MutableList<String>) {

        recyclerView = requireView().findViewById(R.id.listPhotosRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.adapter = SingleCollectionInListAdapter(retrievedCollections.toTypedArray(), this)
    }

    fun getLoggedUser() : String {
        return this.loggedUser
    }

    fun setSelectedCollection(selectedCollection: String) {
        this.selectedCollection = selectedCollection
    }

    fun getSelectedCollection(): String {
        return this.selectedCollection
    }

    fun refresh() {
        this.requireActivity().supportFragmentManager.beginTransaction().detach(this).commitNow()
        this.requireActivity().supportFragmentManager.beginTransaction().attach(this).commitNow()
        UploadUtils.reloadUpdatedCollections(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.loggedUser = this.requireArguments().getString("loggedUsername").toString()

        UploadUtils.reloadUpdatedCollections(this)
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
        val camIcon = view.findViewById<ImageView>(R.id.camIcon)
        val plusIcon = view.findViewById<ImageView>(R.id.plusIcon)
        val addUserIcon = view.findViewById<ImageView>(R.id.addFriendIcon)
        val nearbyIcon = view.findViewById<ImageView>(R.id.nearbyIcon)
        val recommendIcon = view.findViewById<ImageView>(R.id.recommendIcon)

        initialUserLetter.text = this.loggedUser[0].uppercase()

        // Views stored in the layout's plus icon
        val views = arrayOf(
            camIcon,
            addUserIcon,
            recommendIcon,
            nearbyIcon
        )

        plusIcon.setOnClickListener {
            if (!this.animated) {
                for (i in views.indices) {
                    ObjectAnimator.ofFloat(views[i], "translationY", -300f*(i+1)).apply {
                        duration = 500
                        start()
                    }
                }
                this.animated = true

            } else {
                for (element in views) {
                    ObjectAnimator.ofFloat(element, "translationY", 0f).apply {
                        duration = 500
                        start()
                    }
                }
                this.animated = false
            }
        }

        camIcon.setOnClickListener {
            this.openCamera()
        }

        nearbyIcon.setOnClickListener {
            val getLocationIntent = Intent(this.requireActivity(), GetLocationService::class.java)
            getLocationIntent.putExtra("action", "nearby")
            this.requireActivity().startService(getLocationIntent)
        }

        recommendIcon.setOnClickListener {
            val recommendIntent = Intent(this.requireActivity(), RecommendActivity::class.java)

            UploadUtils.retrieveRecommendedPlaces(this)
            startActivity(recommendIntent)
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
    }

    fun openCamera(taggedFriend: String = "") {

        val locationIntent = Intent(this.requireActivity(), GetLocationService::class.java)
        locationIntent.putExtra("action", "camera")

        if (taggedFriend != "") {
            locationIntent.putExtra("tagged_friend", taggedFriend)
        }

        this.requireActivity().startService(locationIntent)
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {

            // Using this tmp variable here, so we can default the real variable whether the user clicks 'upload' or close the window.
            val tmpSelectedCollection = this.selectedCollection
            this.setSelectedCollection("")

            val capturedImage: Bitmap = data?.extras!!["data"] as Bitmap
            val taggedFriend = data.extras!!.get("tagged_friend") as String?
            var imageJson = JSONObject()
            imageJson.put("username", this.loggedUser)
            imageJson.put("lat", this.lastKnownLocation!!.latitude)
            imageJson.put("lon", this.lastKnownLocation!!.longitude)
            imageJson.put("tagged_people", JSONArray().put(taggedFriend))
            imageJson.put("length", 1) // Got from camera, so it must be a single image

            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.info_upload_dialog)
            val imageName = dialog.findViewById<EditText>(R.id.image_tv)
            val collectionName = dialog.findViewById<EditText>(R.id.collection_tv)
            val proceed = dialog.findViewById<Button>(R.id.confirmButton)
            val publicCheck = dialog.findViewById<CheckBox>(R.id.publicCheckBox)
            val city = dialog.findViewById<RadioButton>(R.id.radio_city)
            val mountain = dialog.findViewById<RadioButton>(R.id.radio_mountain)
            val sea = dialog.findViewById<RadioButton>(R.id.radio_sea)

            if (tmpSelectedCollection.isNotEmpty()) {
                collectionName.isEnabled = false
                collectionName.setText(tmpSelectedCollection)
            }

            proceed.setOnClickListener {
                var type = ""

                // Required to avoid visibility issues
                fun setType(selectedType: String) {
                    type = selectedType
                }

                when {
                    city.isChecked -> setType(city.text.toString())
                    mountain.isChecked -> setType(mountain.text.toString())
                    sea.isChecked -> setType(sea.text.toString())
                }

                imageJson.put("image_name", imageName.text.toString())
                imageJson.put("public", publicCheck.isChecked)
                imageJson.put("type", type)

                // User wants to add photo to existing collection, else can choose a name for it
                if (tmpSelectedCollection.isNotEmpty()) {
                    imageJson.put("collection_name", tmpSelectedCollection)

                    UploadUtils.uploadImage(
                        imageJson,
                        capturedImage,
                        this.resources.getString(R.string.base_url)+"/addtoexisting",
                        this
                    )
                } else {
                    imageJson.put("collection_name", collectionName.text.toString())

                    UploadUtils.uploadImage(
                        imageJson,
                        capturedImage,
                        this.resources.getString(R.string.base_url)+"/imageupload",
                        this
                    )
                }
                dialog.dismiss()
            }
            dialog.show()
        }

//        else if (requestCode == 222 && resultCode == Activity.RESULT_OK) {
//            val capturedImage: Bitmap = data?.extras!!["data"] as Bitmap
//            FileManagerUtils.createNewCollection(capturedImage, this, this.lastKnownLocation)
//        }
//        else if(requestCode == 333 && resultCode == Activity.RESULT_OK) {
//            val capturedImage: Bitmap = data?.extras!!["data"] as Bitmap
////            FileManagerUtils.addImageToCollection(this.selectedCollection, capturedImage, this)
////            UploadUtils.addImageToCollection(this.selectedCollection, capturedImage, this)
//        }
//        else if(requestCode == 444 && resultCode == Activity.RESULT_OK) {
//            this.lastKnownLocation = data?.extras!!.get("gps_location") as Location
//        }
//        else if(requestCode == 555 && resultCode == Activity.RESULT_OK) {
//            this.lastKnownLocation = data?.extras!!.get("gps_location") as Location
//            val taggedFriend = data.extras!!.get("tagged_friend") as String?
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            if (taggedFriend != null)
//                intent.putExtra("tagged_friend", taggedFriend)
//            startActivityForResult(intent, 111)
//        }
    }
}