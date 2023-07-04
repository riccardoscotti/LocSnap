package com.example.locsnap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Content of layout/activity_main.xml
        setContentView(R.layout.my_main)

        // Button to allow user to send photo to backend
        val buttonConnect = findViewById<Button>(R.id.connectButton)

        // Manages execution flow after have completed the activity started by pressing the button
        val chooseFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val queue = Volley.newRequestQueue(this@MainActivity)
            val inputStream = uri?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            uploadImage(bitmap, queue)
        }

        buttonConnect.setOnClickListener {
            // Allows user to choose the image(s) to upload
            chooseFile.launch("image/*")
        }
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

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, jsonObject,
            { response ->
                val message = response.getString("message")
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        queue.add(jsonObjectRequest)
    }
}