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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.*
import com.example.locsnap.activities.getLocationService
import com.example.locsnap.activities.recommendActivity
import com.example.locsnap.utils.SingleCollectionInListAdapter
import org.json.JSONArray
import org.json.JSONObject

class ChooseFragment : Fragment() {
    private lateinit var loggedUser : String
    private var selected_collection: String = ""
    private var retrievedCollections = mutableListOf<String>()
    private var last_known_location : Location? = null
    private lateinit var recyclerView: RecyclerView
    private val thisInstance = this
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            last_known_location = intent.extras!!.get("location") as Location

            // Show a dialog to allow user to insert num_photos value
            if (intent.extras!!.getString("action").equals("nearby")) {
                val dialog = Dialog(thisInstance.requireActivity())
                dialog.setContentView(R.layout.nearby_dialog)
                val proceed = dialog.findViewById<Button>(R.id.confirmButton)
                val num_photos_tv = dialog.findViewById<TextView>(R.id.num_photos_tv)

                proceed.setOnClickListener {
                    UploadUtils.showNearestPhotos(num_photos_tv.text.toString().toInt(), last_known_location!!, thisInstance)
                }
                dialog.show()

            } else if (intent.extras!!.getString("action").equals("camera")) {
                var cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 111)
            }
//            else if (intent.extras!!.getString("action").equals("upload")) {// If upload service was successful, refresh the fragment
//                thisInstance.refresh()
//            }
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

    fun setSelectedCollection(selectedCollection: String) {
        this.selected_collection = selectedCollection
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
        val camIcon = view.findViewById<ImageView>(R.id.camIcon)
//        val plusIcon = view.findViewById<ImageView>(R.id.plusIcon)
        val addUserIcon = view.findViewById<ImageView>(R.id.addFriendIcon)
        val nearbyButton = view.findViewById<Button>(R.id.nearbyButton)
        val recommendIcon = view.findViewById<ImageView>(R.id.recommendIcon)

        initialUserLetter.text = this.loggedUser.get(0).uppercase()

        camIcon.setOnClickListener {
            this.openCamera()
        }

        nearbyButton.setOnClickListener {
            val getloc = Intent(this.requireActivity(), getLocationService::class.java)
            getloc.putExtra("action", "nearby")
            this.requireActivity().startService(getloc)
        }

        recommendIcon.setOnClickListener {
            val recommendIntent = Intent(this.requireActivity(), recommendActivity::class.java)

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

        val locationIntent = Intent(this.requireActivity(), getLocationService::class.java)
        locationIntent.putExtra("action", "camera")

        if (taggedFriend != "") {
            locationIntent.putExtra("tagged_friend", taggedFriend)
        }

        this.requireActivity().startService(locationIntent)
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
            var imageJson = JSONObject()
            imageJson.put("username", this.loggedUser)
            imageJson.put("lat", this.last_known_location!!.latitude)
            imageJson.put("lon", this.last_known_location!!.longitude)
            imageJson.put("tagged_people", JSONArray().put(taggedFriend))
            imageJson.put("length", 1) // Got from camera, so it must be a single image

            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.info_upload_dialog)
            val image_name = dialog.findViewById<EditText>(R.id.image_tv)
            val collection_name = dialog.findViewById<EditText>(R.id.collection_tv)
            val proceed = dialog.findViewById<Button>(R.id.confirmButton)
            val publicCheck = dialog.findViewById<CheckBox>(R.id.publicCheckBox)
            val city = dialog.findViewById<RadioButton>(R.id.radio_city)
            val mountain = dialog.findViewById<RadioButton>(R.id.radio_mountain)
            val sea = dialog.findViewById<RadioButton>(R.id.radio_sea)

            if (this.selected_collection.isNotEmpty()) {
                collection_name.setEnabled(false)
                collection_name.setText(this.selected_collection)
            }

            proceed.setOnClickListener {
                var type = ""

                fun setType(selectedType: String) {
                    type = selectedType
                }

                if (city.isChecked)
                    setType(city.text.toString())

                else if (mountain.isChecked)
                    setType(mountain.text.toString())

                else if (sea.isChecked)
                    setType(sea.text.toString())

                imageJson.put("image_name", image_name.text.toString())
                imageJson.put("public", publicCheck.isChecked)
                imageJson.put("type", type)

                if (this.selected_collection.isNotEmpty())
                    imageJson.put("collection_name", this.selected_collection)
                else
                    imageJson.put("collection_name", collection_name.text.toString())

                if (this.selected_collection.isNotEmpty()) {
                    UploadUtils.upload2(
                        imageJson,
                        capturedImage,
                        this.resources.getString(R.string.base_url)+"/addtoexisting",
                        this,
                        taggedFriend
                    )
                    setSelectedCollection("") // Reset selected_collection for new usage
                } else {
                    UploadUtils.upload2(
                        imageJson,
                        capturedImage,
                        this.resources.getString(R.string.base_url)+"/imageupload",
                        this,
                        taggedFriend
                    )
                }
                dialog.dismiss()
            }

            dialog.show()

        } else if (requestCode == 222 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data?.extras!!["data"] as Bitmap
            FileManagerUtils.createNewCollection(capturedImage, this, this.last_known_location)
        } else if(requestCode == 333 && resultCode == Activity.RESULT_OK) {
            val capturedImage: Bitmap = data?.extras!!["data"] as Bitmap
//            FileManagerUtils.addImageToCollection(this.selected_collection, capturedImage, this)
//            UploadUtils.addImageToCollection(this.selected_collection, capturedImage, this)
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