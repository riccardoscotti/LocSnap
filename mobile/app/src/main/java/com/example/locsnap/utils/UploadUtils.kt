package com.example.locsnap

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
         *
         * @param bitmap The bitmap that needs to be sent to backend.
         * @param queue The requests queue containing them
         */
        fun uploadImage(bitmap: Bitmap?, logged_user: String, shared_by: String, fragment: ChooseFragment) {

            // Creazione coda per le richieste Volley
            val queue = Volley.newRequestQueue(fragment.requireActivity().applicationContext)

            // ByteArray in cui verrà convertita la bitmap, per poter essere rappresentata in un db
            val byteArrayOutputStream = ByteArrayOutputStream()

            // Compressione bitmap
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

            // Conversione in un formato Base64
            val image: String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

            val url = "${fragment.resources.getString(R.string.base_url)}/imageupload"
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val formattedDateTime = currentDateTime.format(formatter)
            val name: String = "IMG_" + formattedDateTime
            val location = fragment.getLastKnownLocation()

            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("image", JSONArray().put(image))
            jsonObject.put("username", logged_user)
            jsonObject.put("tagged_people", JSONArray())
            jsonObject.put("lat", location?.latitude)
            jsonObject.put("lon", location?.longitude)
            jsonObject.put("length", 1)

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

        fun uploadCollection(file: File, fragment: Fragment) {

            val url = "${fragment.resources.getString(R.string.base_url)}/imageupload"
            val queue = Volley.newRequestQueue(fragment.context)
            val json = JSONObject()
            val date = SimpleDateFormat("yyyy-MM-dd").format(Date(file.lastModified()))

            val location = FileManagerUtils.getCollections().get(file.absolutePath)

            var bitmaps = JSONArray()
            for ((index, bitmap) in file.readText().split(",").withIndex()) {
                bitmaps.put(index, bitmap)
            }

            json.put("name", file.name)
            //json.put("date", date)
            json.put("image", bitmaps)
            json.put("lat", location?.latitude)
            json.put("lon", location?.longitude)

            Log.d("collection", json.toString())

            val sendCollectionRequest = object : JsonObjectRequest(
                Method.POST,
                url,
                json,
                { response ->
                    if (response.getString("status").equals("200")) {
                        Toast.makeText(fragment.activity, "Collection successfully sent.", Toast.LENGTH_SHORT).show()
                    }
                    else
                        Toast.makeText(fragment.activity, "[IMAGE] Problem occurred during collection sending process.", Toast.LENGTH_SHORT).show()
                }, {
                    Toast.makeText(fragment.activity, "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(sendCollectionRequest)
        }
    }
}