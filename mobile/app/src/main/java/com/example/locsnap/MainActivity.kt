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
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenuto del file layout/activity_main.xml
        setContentView(R.layout.my_main)

        val buttonConnect = findViewById<Button>(R.id.connectButton)
        val textView = findViewById<TextView>(R.id.textView)

        val queue = Volley.newRequestQueue(this@MainActivity)
        
        val chooseFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

            val inputStream = uri?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            uploadImage(bitmap, queue)

            /*val cursor = uri?.let { contentResolver.query(it, null, null, null, null) }
            cursor?.use {
                it.moveToFirst()
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val fileName = it.getString(nameIndex)
                textView.text = fileName
            }*/

        }

        buttonConnect.setOnClickListener {
            // Let user choose photo to be uploaded
            chooseFile.launch("image/*")
        }
    }

    private fun uploadImage(bitmap: Bitmap?, queue: RequestQueue) {
        /*val byteArrayOutputStream = ByteArrayOutputStream()
        val textView = findViewById<TextView>(R.id.textView)
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val image: String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)*/
        //Toast.makeText(this@MainActivity, image.length.toString(), Toast.LENGTH_SHORT).show()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val image: String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        val url = getString(R.string.base_url) + "/imageupload"
        val name: String = java.lang.String.valueOf(Calendar.getInstance().getTimeInMillis())

        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("image", image)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                val message = response.getString("message")
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(this@MainActivity, "Image upload failed.", Toast.LENGTH_SHORT).show()
                val textView = findViewById<TextView>(R.id.textView)
                textView.text = it.message
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        queue.add(jsonObjectRequest)
    }

}