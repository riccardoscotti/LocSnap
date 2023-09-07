package com.example.locsnap

import android.content.Intent
import android.graphics.Bitmap
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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

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
        fun uploadImage(bitmap: Bitmap?, logged_user: String, fragment: ChooseFragment) {

            // ByteArray in cui verrà convertita la bitmap, per poter essere rappresentata in un db
            val byteArrayOutputStream = ByteArrayOutputStream()

            // Compressione bitmap
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

            // Conversione in un formato Base64
            val bitmap: String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

            val url = "${fragment.resources.getString(R.string.base_url)}/imageupload"
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val formattedDateTime = currentDateTime.format(formatter)
            val name: String = "IMG_" + formattedDateTime
            val location = fragment.getLastKnownLocation()

            val jsonObject = JSONObject()
            var bitmaps = JSONArray()
            bitmaps.put(0, bitmap) // Foto singola

            jsonObject.put("name", name)
            jsonObject.put("bitmaps", bitmaps)
            jsonObject.put("username", logged_user)
            jsonObject.put("lat", location?.latitude)
            jsonObject.put("lon", location?.longitude)
            jsonObject.put("tagged_people", JSONArray()) // Inizializzo sempre vuoto
            jsonObject.put("lenght", 1) // Foto singola

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
            json.put("bitmaps", bitmaps)
            json.put("username", fragment.getLoggedUser())
            json.put("lat", location?.latitude)
            json.put("lon", location?.longitude)
            json.put("tagged_people", JSONArray()) // Inizializzo sempre vuoto
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

        fun showNearestPhotos(n: Int, fragment: ChooseFragment) {

            fragment.startActivityForResult(Intent(fragment.requireContext(), getLocationActivity::class.java), 777)
            fragment.getLastKnownLocation()

        }
    }
}