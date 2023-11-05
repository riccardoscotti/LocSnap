package com.example.locsnap.utils

import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.util.Base64
import android.widget.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import com.example.locsnap.R
import com.example.locsnap.activities.RecommendActivity
import com.example.locsnap.fragments.ChooseFragment
import com.example.locsnap.activities.ShowImagesActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Exception

class UploadUtils {
    companion object {
//        fun uploadImage(capturedImage: Bitmap, logged_user: String, fragment: ChooseFragment, taggedFriend: String? = "") {
//
//            // ByteArray in cui verrà convertita la bitmap, per poter essere rappresentata in un db
//            val bitmapBA = ByteArrayOutputStream()
//
//            // Compressione bitmap
//            capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, bitmapBA)
//
//            // Leggi i dati dall'InputStream e convertili in una stringa codificata in base64
//            val bitmapEncoded = Base64.encodeToString(bitmapBA.toByteArray(), Base64.DEFAULT)
//
//            val url = "${fragment.resources.getString(R.string.base_url)}/imageupload"
//            val currentDateTime = LocalDateTime.now()
//            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
//            val formattedDateTime = currentDateTime.format(formatter)
//            val name: String = "IMG_" + formattedDateTime
//            val location = fragment.getLastKnownLocation()
//            val bitmaps = JSONArray()
//            bitmaps.put(bitmapEncoded)
//
//            val jsonObject = JSONObject()
//
//            jsonObject.put("name", name)
//
//            jsonObject.put("image", bitmaps)
//            jsonObject.put("username", logged_user)
//            jsonObject.put("lat", location?.latitude)
//            jsonObject.put("lon", location?.longitude)
//            jsonObject.put("length", 1)
//
//            if (taggedFriend != "") {
//                jsonObject.put("tagged_people", JSONArray().put(taggedFriend))
//            } else
//                jsonObject.put("tagged_people", JSONArray())
//
//            upload(url, jsonObject, fragment)
//        }

        /*
        * Allows the upload of a collection of photos.
        * N.B. A single photo, uploaded from the photo picker, will be treated as a collection with one photo.
        * */
//        fun uploadCollection(file: File, fragment: ChooseFragment) {
//
//            val url = "${fragment.resources.getString(R.string.base_url)}/imageupload"
//            val location = FileManagerUtils.getCollections().get(file.absolutePath)
//            var bitmaps = JSONArray()
//            val json = JSONObject()
//
//            for ((index, bitmap) in file.readText().split(",").withIndex()) {
//                bitmaps.put(index, bitmap)
//            }
//
//            json.put("name", file.name)
//            json.put("image", bitmaps)
//            json.put("username", fragment.getLoggedUser())
//            json.put("lat", location?.latitude)
//            json.put("lon", location?.longitude)
//            json.put("tagged_people", JSONArray())
//            json.put("length", bitmaps.length())
//
//            upload(url, json, fragment)
//        }

//        fun upload(url: String, jsonObject: JSONObject, fragment: ChooseFragment) {
//
//            val queue = Volley.newRequestQueue(fragment.requireActivity())
//            val apiKey = "01114512c1ce49018d40d94d6aab3d68"
//
//            val placeURL =
//                "https://api.geoapify.com/v1/geocode/reverse?lat=${jsonObject.get("lat")}&lon=${jsonObject.get("lon")}&apiKey=${apiKey}"
//
//            val placeRequest = object : JsonObjectRequest(
//                Method.GET, placeURL, null,
//                { response ->
//                    val place: String = response.getJSONArray("features")
//                        .getJSONObject(0)
//                        .getJSONObject("properties")
//                        .getString("city")
//
//                    if (place != "") {
//                        val dialog = Dialog(fragment.requireActivity())
//                        dialog.setContentView(R.layout.info_upload_dialog)
//
//                        val proceed = dialog.findViewById<Button>(R.id.confirmButton)
//                        val publicCheck = dialog.findViewById<CheckBox>(R.id.publicCheckBox)
//                        val city = dialog.findViewById<RadioButton>(R.id.radio_city)
//                        val mountain = dialog.findViewById<RadioButton>(R.id.radio_mountain)
//                        val sea = dialog.findViewById<RadioButton>(R.id.radio_sea)
//
//                        proceed.setOnClickListener {
//
//                            var type = ""
//
//                            fun setType(selectedType: String) {
//                                type = selectedType
//                            }
//
//                            if (city.isChecked)
//                                setType(city.text.toString())
//
//                            else if (mountain.isChecked)
//                                setType(mountain.text.toString())
//
//                            else if (sea.isChecked)
//                                setType(sea.text.toString())
//
//                            if (type == "") {
//                                Toast.makeText(fragment.requireActivity(), "You must choose a type.", Toast.LENGTH_SHORT).show()
//                            } else {
//                                jsonObject.put("public", publicCheck.isChecked)
//                                jsonObject.put("type", type)
//                                jsonObject.put("place", place)
//
//                                val sendRequest = object : JsonObjectRequest(
//
//                                    Method.POST, url, jsonObject,
//                                    { response ->
//                                        if (response.getString("status").equals("200")) {
//                                            Toast.makeText(
//                                                fragment.requireActivity(),
//                                                "Image successfully sent.",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                            fragment.refresh()
//
//                                        } else {
//                                            Toast.makeText(
//                                                fragment.requireActivity(),
//                                                "[IMAGE] Problem occurred during image sending process.",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                    },
//                                    {
//                                        Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT)
//                                            .show()
//                                    }
//                                ) {
//                                    override fun getBodyContentType(): String {
//                                        return "application/json; charset=utf-8"
//                                    }
//                                }
//                                queue.add(sendRequest)
//                                dialog.dismiss()
//                            }
//                        }
//                        dialog.show()
//                    } else
//                        Toast.makeText(
//                            fragment.requireActivity(),
//                            "[IMAGE] Problem occurred during reverse geocoding process.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                },
//                {
//                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
//                }
//            ) {
//                override fun getBodyContentType(): String {
//                    return "application/json; charset=utf-8"
//                }
//            }
//
//            queue.add(placeRequest)
//        }

        fun showNearestPhotos(numPhotos: Int, actualPos: Location, fragment: ChooseFragment) {

            val url : String = fragment.resources.getString(R.string.base_url)+"/nearest"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("actual_lat", actualPos.latitude)
            jsonObject.put("actual_lon", actualPos.longitude)
            jsonObject.put("num_photos", numPhotos)

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

                        if (numPhotos > jsonLength) {
                            Toast.makeText(
                                fragment.requireContext(),
                                "Insufficient number of photos uploaded! Only $jsonLength will be shown.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        val intent = Intent(fragment.requireContext(), ShowImagesActivity::class.java)
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

        fun tagFriend(friend: String, imageName: String, fragment: ChooseFragment) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/tag_friend"
            val jsonObject = JSONObject()
            jsonObject.put("logged_user", fragment.getLoggedUser())
            jsonObject.put("friend", friend)
            jsonObject.put("image_name", imageName)

            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)
            val tagFriendRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        Toast.makeText(fragment.requireActivity(), "$friend tagged successfully!", Toast.LENGTH_SHORT).show()
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

        fun reloadUpdatedCollections(fragment: ChooseFragment) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/retrievecollections"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", fragment.getLoggedUser())

            val retrieveRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        val retrievedCollections = response.getJSONArray("retrieved_collections")
                        val userCollections = mutableListOf<String>()

                        for (i in 0 until retrievedCollections.length()) {
                            userCollections.add(retrievedCollections.getJSONObject(i).getString("name"))
                        }

                        fragment.setCollections(userCollections)
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

        fun deleteCollection(collectionName: String, fragment: ChooseFragment) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/deletecollection"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", fragment.getLoggedUser())
            jsonObject.put("collection_name", collectionName)

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

                        val intent = Intent(fragment.requireContext(), RecommendActivity::class.java)
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

        fun imagesOfCollection(fragment: ChooseFragment, adapter: SingleCollectionInListAdapter, collectionName: String) : Boolean {
            val url: String = fragment.resources.getString(R.string.base_url) + "/imagesof"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", fragment.getLoggedUser())
            jsonObject.put("collection_name", collectionName)

            val future = RequestFuture.newFuture<JSONObject>()
            val newRequest = object : JsonObjectRequest(Method.POST, url, jsonObject, future, future) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(newRequest)

            try {
                    val response: JSONObject = future.get()

                    if (response.getString("status").equals("200")) {
                        val retrievedImages = response.getJSONObject("images")
                        var images = JSONObject()

                        for (i in 0 until retrievedImages.length()) {
                            images.put("$i", retrievedImages.getJSONObject("$i"))
                        }

                        adapter.setImagesList(images)

                    } else {
                        Toast.makeText(fragment.requireActivity(), "Error during images retrieval.", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: Exception) {
                Toast.makeText(fragment.requireContext(), "Errore nel future request", Toast.LENGTH_LONG).show()
            }

            return true
        }

        fun updateImageInfo(fragment: ChooseFragment, oldImageName: String, imageName: String, collectionName: String, public: Boolean, type: String) {
            val url : String = fragment.resources.getString(R.string.base_url)+"/updateimage"
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val jsonObject = JSONObject()
            jsonObject.put("logged_user", fragment.getLoggedUser())
            jsonObject.put("old_image_name", oldImageName)
            jsonObject.put("new_image_name", imageName)
            jsonObject.put("collection_name", collectionName)
            jsonObject.put("public", public)
            jsonObject.put("type", type)

            val retrieveRequest = object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        Toast.makeText(fragment.requireActivity(), "Image updated correctly.", Toast.LENGTH_SHORT).show()
                    } else if (response.getString("status").equals("401")) {
                        Toast.makeText(fragment.requireActivity(), "Error during image update. Re-check image informations", Toast.LENGTH_SHORT).show()
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

        fun uploadImage(imageJson: JSONObject, capturedImage: Bitmap, url: String, fragment: ChooseFragment) {

            val bitmapBA = ByteArrayOutputStream()
            capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, bitmapBA)
            val image64 = Base64.encodeToString(bitmapBA.toByteArray(), Base64.DEFAULT)
            imageJson.put("image", image64)

            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val apiKey = "01114512c1ce49018d40d94d6aab3d68"
            val placeURL =
                "https://api.geoapify.com/v1/geocode/reverse?lat=${imageJson.get("lat")}&lon=${imageJson.get("lon")}&apiKey=${apiKey}"

            val placeRequest = object : JsonObjectRequest (
                Method.GET, placeURL, null,
                { response ->
                    val place: String = response.getJSONArray("features")
                        .getJSONObject(0)
                        .getJSONObject("properties")
                        .getString("city")

                    imageJson.put("place", place)

                    val uploadRequest = object : JsonObjectRequest(
                        Method.POST, url, imageJson,
                        { response ->
                            if (response.getString("status").equals("200")) {
                                Toast.makeText(fragment.requireActivity(), "Image uploaded successfully.", Toast.LENGTH_SHORT).show()
                            } else if (response.getString("status").equals("401")) {
                                Toast.makeText(fragment.requireActivity(), "Error during image upload.", Toast.LENGTH_SHORT).show()
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
                    queue.add(uploadRequest)
                    fragment.refresh()
                }, {}
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }

            queue.add(placeRequest)
        }
    }
}
