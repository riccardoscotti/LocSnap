package com.example.locsnap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenuto del file layout/activity_main.xml
        setContentView(R.layout.my_activity_main)

        setSupportActionBar(findViewById(R.id.my_toolbar))

        val navController = findNavController(R.id.my_content_main)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Allows the image uploading to backend.
     * It sends, using Volley, a JSONRequest having the structure {name: IMG_yyyyMMdd_HHmmss, image: byteArray}
     *
     * @param bitmap, the bitmap that needs to be sent to backend.
     * @param queue, the requests queue containing them
     */
    private fun uploadImage(bitmap: Bitmap?, queue: RequestQueue) {
      
        // Create a ByteArrayOutputStream object to write the bitmap image data to a byte array
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Compress the bitmap image to JPEG format and write the compressed data to the ByteArrayOutputStream object
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        // Encode the byte array to a Base64 string
        val image: String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

        val url = getString(R.string.base_url) + "/imageupload"
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
                    Toast.makeText(this@MainActivity, "Image successfully sent.", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@MainActivity, "[IMAGE] Communication error.", Toast.LENGTH_SHORT).show()
            },
            {

                Toast.makeText(this@MainActivity, "Image upload failed.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }
        queue.add(sendImageRequest)
    }
}