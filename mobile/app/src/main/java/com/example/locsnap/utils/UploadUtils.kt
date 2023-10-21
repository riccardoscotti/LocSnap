package com.example.locsnap

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.provider.MediaStore.Audio.Radio
import android.util.Base64
import android.util.Log
import android.widget.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.locsnap.activities.recommendActivity
import com.example.locsnap.fragments.ChooseFragment
import com.example.locsnap.utils.SingleCollectionInListAdapter
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class UploadUtils {
    companion object {

        /**
         * Allows the image uploading to backend.
         * It sends, using Volley, a JSONRequest having the structure {name: IMG_yyyyMMdd_HHmmss, image: byteArray}
         * It is called when users uploads a single photo just taken with the camera.
         *
         * @param bitmap The bitmap that needs to be sent to backend.
         * @param queue The requests queue containing them
         */
        fun uploadImage(capturedImage: Bitmap, logged_user: String, fragment: ChooseFragment, taggedFriend: String? = "") {

            // ByteArray in cui verrà convertita la bitmap, per poter essere rappresentata in un db
            val bitmapBA = ByteArrayOutputStream()

            // Compressione bitmap
            capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, bitmapBA)

            // Leggi i dati dall'InputStream e convertili in una stringa codificata in base64
            val bitmapEncoded = Base64.encodeToString(bitmapBA.toByteArray(), Base64.DEFAULT)

            val url = "${fragment.resources.getString(R.string.base_url)}/imageupload"
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val formattedDateTime = currentDateTime.format(formatter)
            val name: String = "IMG_" + formattedDateTime
            val location = fragment.getLastKnownLocation()
            val bitmaps = JSONArray()
            bitmaps.put(bitmapEncoded)

            val jsonObject = JSONObject()

            jsonObject.put("name", name)

            jsonObject.put("image", bitmaps)
            jsonObject.put("username", logged_user)
            jsonObject.put("lat", location?.latitude)
            jsonObject.put("lon", location?.longitude)
            jsonObject.put("length", 1)

            if (taggedFriend != "") {
                jsonObject.put("tagged_people", JSONArray().put(taggedFriend))
            } else
                jsonObject.put("tagged_people", JSONArray())

            upload(url, jsonObject, fragment)
        }

        /*
        * Allows the upload of a collection of photos.
        * N.B. A single photo, uploaded from the photo picker, will be treated as a collection with one photo.
        * */
        fun uploadCollection(file: File, fragment: ChooseFragment) {

            val url = "${fragment.resources.getString(R.string.base_url)}/imageupload"
            val location = FileManagerUtils.getCollections().get(file.absolutePath)
            var bitmaps = JSONArray()
            val json = JSONObject()

            for ((index, bitmap) in file.readText().split(",").withIndex()) {
                bitmaps.put(index, bitmap)
            }

            json.put("name", file.name)
            json.put("image", bitmaps)
            json.put("username", fragment.getLoggedUser())
            json.put("lat", location?.latitude)
            json.put("lon", location?.longitude)
            json.put("tagged_people", JSONArray())
            json.put("length", bitmaps.length())

            upload(url, json, fragment)
        }

        fun upload(url: String, jsonObject: JSONObject, fragment: ChooseFragment) {

            val queue = Volley.newRequestQueue(fragment.requireActivity())
            val apiKey = "01114512c1ce49018d40d94d6aab3d68"

            val placeURL =
                "https://api.geoapify.com/v1/geocode/reverse?lat=${jsonObject.get("lat")}&lon=${jsonObject.get("lon")}&apiKey=${apiKey}"

            val placeRequest = object : JsonObjectRequest(
                Method.GET, placeURL, null,
                { response ->
                    val place: String = response.getJSONArray("features")
                        .getJSONObject(0)
                        .getJSONObject("properties")
                        .getString("city")

                    if (place != "") {
                        val dialog = Dialog(fragment.requireActivity())
                        dialog.setContentView(R.layout.info_upload_dialog)

                        val proceed = dialog.findViewById<Button>(R.id.confirmButton)
                        val publicCheck = dialog.findViewById<CheckBox>(R.id.publicCheckBox)
                        val city = dialog.findViewById<RadioButton>(R.id.radio_city)
                        val mountain = dialog.findViewById<RadioButton>(R.id.radio_mountain)
                        val sea = dialog.findViewById<RadioButton>(R.id.radio_sea)

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

                            if (type == "") {
                                Toast.makeText(fragment.requireActivity(), "You must choose a type.", Toast.LENGTH_SHORT).show()
                            } else {
                                jsonObject.put("public", publicCheck.isChecked)
                                jsonObject.put("type", type)
                                jsonObject.put("place", place)

                                val sendRequest = object : JsonObjectRequest(

                                    Method.POST, url, jsonObject,
                                    { response ->
                                        if (response.getString("status").equals("200")) {
                                            Toast.makeText(
                                                fragment.requireActivity(),
                                                "Image successfully sent.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            fragment.refresh()

                                        } else {
                                            Toast.makeText(
                                                fragment.requireActivity(),
                                                "[IMAGE] Problem occurred during image sending process.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    {
                                        Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                ) {
                                    override fun getBodyContentType(): String {
                                        return "application/json; charset=utf-8"
                                    }
                                }
                                queue.add(sendRequest)
                                dialog.dismiss()
                            }
                        }
                        dialog.show()
                    } else
                        Toast.makeText(
                            fragment.requireActivity(),
                            "[IMAGE] Problem occurred during reverse geocoding process.",
                            Toast.LENGTH_SHORT
                        ).show()
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }

            queue.add(placeRequest)
        }

        fun showNearestPhotos(num_photos: Int, actualPos: Location, fragment: ChooseFragment) {

            val url : String = fragment.resources.getString(R.string.base_url)+"/nearest"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("actual_lat", actualPos.latitude)
            jsonObject.put("actual_lon", actualPos.longitude)
            jsonObject.put("num_photos", num_photos)

            val sendCollectionRequest = object : JsonObjectRequest(
                Method.POST,
                url,
                jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        val receivedImagesString = response.getString("images")
                        val jsonParsed = JSONArray(receivedImagesString)
                        val jsonLength = jsonParsed.length()
                        var bitmapsReceived = mutableListOf<String>()
                        for (i in 0 until jsonLength) {
                            bitmapsReceived.add(jsonParsed.getString(i))
                        }
                        if (num_photos > jsonLength) {
                            Toast.makeText(
                                fragment.requireContext(),
                                "Insufficient number of photos uploaded! Only ${jsonLength} will be shown.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        val intent = Intent(fragment.requireContext(), showImagesActivity::class.java)
                        intent.putExtra("imagesString", bitmapsReceived.toTypedArray())
                        fragment.startActivity(intent)

                    } else {
                        Toast.makeText(
                            fragment.requireActivity(),
                            "Unable to locate nearby photos!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(sendCollectionRequest)
        }

        fun tagFriend(logged_user: String, friend: String, collection_name: String, fragment: ChooseFragment) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/tag_friend"
            val jsonObject = JSONObject()
            jsonObject.put("logged_user", logged_user)
            jsonObject.put("friend", friend)
            jsonObject.put("collection_name", collection_name)

            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)
            val tagFriendRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        Toast.makeText(fragment.requireActivity(), "${friend} tagged successfully!", Toast.LENGTH_SHORT).show()
                    } else if (response.getString("status").equals("304")) {
                        Toast.makeText(fragment.requireActivity(), "You must upload the collection first!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(fragment.requireActivity(),"Error during tag process...",Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(tagFriendRequest)
        }

        fun reloadUpdatedCollections(logged_user: String, fragment: ChooseFragment) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/retrievecollections"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", logged_user)

            val retrieveRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        val retrievedCollections = response.getJSONArray("retrieved_collections")
                        val user_collections = mutableListOf<String>()

                        for (i in 0 until retrievedCollections.length()) {
                            user_collections.add(retrievedCollections.get(i).toString())
                        }

                        fragment.setCollections(user_collections)
                    }
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(retrieveRequest)
        }

        fun deleteCollection(logged_user: String, collection_name: String, fragment: ChooseFragment) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/deletecollection"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", logged_user)
            jsonObject.put("collection_name", collection_name)

            val deleteRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                {
                    fragment.refresh()
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(deleteRequest)
        }

        fun retrieveRecommendedPlaces(fragment: ChooseFragment) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/recommend"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", fragment.getLoggedUser())

            // Edit this...
            val recommendRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        val retrievedPlaces = response.getJSONArray("recommendedPlaces")
                        var recommendedPlaces : MutableList<String> = mutableListOf()

                        for (i in 0 until retrievedPlaces.length()) {
                            recommendedPlaces.add(retrievedPlaces.get(i).toString())
                        }

                        val intent = Intent(fragment.requireContext(), recommendActivity::class.java)
                        intent.putExtra("recommended_places", recommendedPlaces.toTypedArray())
                        fragment.startActivity(intent)

                    } else if (response.getString("status").equals("409")) {
                        Toast.makeText(fragment.requireActivity(), "No available places to be recommended.", Toast.LENGTH_LONG).show()
                    }
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(recommendRequest)
        }

        fun imagesOfCollection(fragment: ChooseFragment, adapter: SingleCollectionInListAdapter, collection_name: String) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/imagesof"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", fragment.getLoggedUser())
            jsonObject.put("collection_name", collection_name)

            val retrieveRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        val retrieved_images = response.getJSONArray("images")
                        var images = mutableListOf<String>()

                        for (i in 0 until retrieved_images.length()) {
                            images.add(retrieved_images.get(i).toString())
                        }

                        adapter.setImagesList(images.toTypedArray())

                    } else if (response.getString("status").equals("401")) {
                        Toast.makeText(fragment.requireActivity(), "Error during images retrieval.", Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(retrieveRequest)
        }

        fun updateImageInfo(fragment: ChooseFragment, image_name: String, public: Boolean, type: String) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/updateimage"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", fragment.getLoggedUser())
            jsonObject.put("image_name", image_name)
            jsonObject.put("public", public)
            jsonObject.put("type", type)

            val retrieveRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        Toast.makeText(fragment.requireActivity(), "Image updated correctly.", Toast.LENGTH_SHORT).show()
                    } else if (response.getString("status").equals("401")) {
                        Toast.makeText(fragment.requireActivity(), "Error during image update.", Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(retrieveRequest)
        }
    }
}
