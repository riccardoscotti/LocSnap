package com.example.locsnap

import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.locsnap.fragments.ChooseFragment
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

            Toast.makeText(fragment.requireActivity(), "Amico: ${taggedFriend}", Toast.LENGTH_SHORT)

            // Compressione bitmap
            capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, bitmapBA)

            // Leggi i dati dall'InputStream e convertili in una stringa codificata in base64
            val bitmapEncoded = Base64.encodeToString(bitmapBA.toByteArray(), Base64.DEFAULT)

            Log.d("image", "size: ${bitmapEncoded.length}")

            val url = "${fragment.resources.getString(R.string.base_url)}/imageupload"
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val formattedDateTime = currentDateTime.format(formatter)
            val name: String = "IMG_" + formattedDateTime
            val location = fragment.getLastKnownLocation()
            var bitmaps = JSONArray()
            bitmaps.put(bitmapEncoded)

            val jsonObject = JSONObject()

            jsonObject.put("name", name)
            jsonObject.put("image", bitmaps)
            jsonObject.put("username", logged_user)
            jsonObject.put("lat", location?.latitude)
            jsonObject.put("lon", location?.longitude)
            jsonObject.put("lenght", 1) // Foto singola

            if (taggedFriend != "") {
                jsonObject.put("tagged_people", JSONArray().put(taggedFriend))
            }

            else
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
            json.put("lenght", bitmaps.length())

            upload(url, json, fragment)
        }

        fun upload(url: String, jsonObject: JSONObject, fragment: Fragment) {

            // Creazione coda per le richieste Volley
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            val sendImageRequest = object : JsonObjectRequest(

                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200"))
                        Toast.makeText(fragment.requireActivity(), "Image successfully sent.", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(fragment.requireActivity(), "[IMAGE] Problem occurred during image sending process.", Toast.LENGTH_SHORT).show()
                },
                {
                    Toast.makeText(fragment.requireActivity(), "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(sendImageRequest)
        }

        fun showNearestPhotos(num_photos: Int, actualPos: Location, fragment: ChooseFragment) {

            val url : String = fragment.resources.getString(R.string.base_url)+"/nearest"
            val jsonObject = JSONObject()
            jsonObject.put("actual_lat", actualPos.latitude)
            jsonObject.put("actual_lon", actualPos.longitude)
            jsonObject.put("num_photos", num_photos)

            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)
            val sendImageRequest = object : JsonObjectRequest(

                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        var receivedImagesString = response.getString("images")
                        var jsonParsed = JSONArray(receivedImagesString)
                        var bitmapsReceived = mutableListOf<String>()
                        for (i in 0 until num_photos) {
                            bitmapsReceived.add(jsonParsed.getString(i))
                        }
                        val intent = Intent(fragment.requireContext(), showImagesActivity::class.java)
                        intent.putExtra("imagesString", bitmapsReceived.toTypedArray())
                        fragment.startActivity(intent)

                    } else {
                        Toast.makeText(
                            fragment.requireActivity(),
                            "Error during location retrieval...",
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
            queue.add(sendImageRequest)
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
    }
}