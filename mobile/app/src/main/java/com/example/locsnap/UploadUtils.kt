package com.example.locsnap

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class UploadUtils {
    companion object {

        /**
         * Allows the image uploading to backend.
         * It sends, using Volley, a JSONRequest having the structure {name: IMG_yyyyMMdd_HHmmss, image: byteArray}
         *
         * @param bitmap The bitmap that needs to be sent to backend.
         * @param queue The requests queue containing them
         */
        fun uploadImage(bitmap: Bitmap?, logged_user: String, shared_by: String, activity: Activity) {

            // Creates queue based on fragment's context
            val queue = Volley.newRequestQueue(activity.applicationContext)

            // Create a ByteArrayOutputStream object to write the bitmap image data to a byte array
            val byteArrayOutputStream = ByteArrayOutputStream()

            // Compress the bitmap image to JPEG format and write the compressed data to the ByteArrayOutputStream object
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

            // Encode the byte array to a Base64 string
            val image: String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

            val url = "http://10.0.2.2:8080/imageupload"
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val formattedDateTime = currentDateTime.format(formatter)
            val name: String = "IMG_" + formattedDateTime

            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("image", image)
            jsonObject.put("username", logged_user)
            jsonObject.put("shared_by", shared_by)

            val sendImageRequest = object : JsonObjectRequest(

                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200"))
                        Toast.makeText(activity, "Image successfully sent.", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(activity, "[IMAGE] Problem occurred during image sending process.", Toast.LENGTH_SHORT).show()
                },
                {
                    Toast.makeText(activity, "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(sendImageRequest)
        }

        fun uploadCollection(file: File, fragment: Fragment) {

            val url = "http://10.0.2.2:8080/collectionupload"
            val queue = Volley.newRequestQueue(fragment.context)
            val json = JSONObject()
            val date = SimpleDateFormat("yyyy-MM-dd").format(Date(file.lastModified()))

            var bitmaps = JSONArray()
            for ((index, bitmap) in file.readText().split(",").withIndex()) {
                bitmaps.put(index, bitmap)
            }

            json.put("name", file.name)
            json.put("date", date)
            json.put("bitmaps", bitmaps)

            Log.d("collection", json.toString())

            val sendCollectionRequest = object : JsonObjectRequest(
                Method.POST,
                url,
                json,
                { response ->
                    if (response.getString("status").equals("200"))
                        Toast.makeText(fragment.activity, "Collection successfully sent.", Toast.LENGTH_SHORT).show()
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