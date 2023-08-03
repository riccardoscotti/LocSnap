package com.example.locsnap

import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UploadUtils {
    companion object {

        /**
         * Allows the image uploading to backend.
         * It sends, using Volley, a JSONRequest having the structure {name: IMG_yyyyMMdd_HHmmss, image: byteArray}
         *
         * @param bitmap, the bitmap that needs to be sent to backend.
         * @param queue, the requests queue containing them
         */
        fun uploadImage(bitmap: Bitmap?, fragment : Fragment) {

            // Creates queue based on fragment's context
            val queue = Volley.newRequestQueue(fragment.context)

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

            val sendImageRequest = object : JsonObjectRequest(

                Method.POST, url, jsonObject,
                { response ->
                    if (response.getString("status").equals("200"))
                        Toast.makeText(fragment.activity, "Image successfully sent.", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(fragment.activity, "[IMAGE] Problem occurred during image sending process.", Toast.LENGTH_SHORT).show()
                },
                {

                    Toast.makeText(fragment.activity, "Communication error.", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            queue.add(sendImageRequest)
        }
    }
}