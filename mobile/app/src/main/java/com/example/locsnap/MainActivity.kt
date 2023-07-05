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
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
<<<<<<< Updated upstream
import java.util.*
=======
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
>>>>>>> Stashed changes

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenuto del file layout/activity_main.xml
        setContentView(R.layout.my_main)

        val buttonConnect = findViewById<Button>(R.id.connectButton)
        val textView = findViewById<TextView>(R.id.textView)

        val queue = Volley.newRequestQueue(this@MainActivity)
        
        val chooseFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
<<<<<<< Updated upstream
=======
            val queue = Volley.newRequestQueue(this@MainActivity)
            userLogin("ChristianP01", "christian123", queue)
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
=======
    private fun encrypt(pass: String, key: String): String {
        val iv = "1234567890123456".toByteArray(charset("utf-8"))
        val skeySpec = SecretKeySpec(key.toByteArray(charset("utf-8")), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(pass.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    private fun userLogin(username: String, password: String, queue: RequestQueue) {
        val enc_key = "locsnap-project-enckey2023"
        //val enc_pwd = encrypt(password, enc_key)

        val jsonObject = JSONObject()
        jsonObject.put("username", username)
        //jsonObject.put("password", enc_pwd)
        jsonObject.put("password", password)

        val jsonRequest = JsonObjectRequest(
            Request.Method.POST,
            getString(R.string.base_url) + "/login",
            jsonObject,
            { response ->
                if (response.getString("status").equals("200"))
                    Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@MainActivity, "Login error.", Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(this@MainActivity, "[LOGIN] Communication error.", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonRequest)
    }

    /**
     * Allows the image uploading to backend.
     * It sends, using Volley, a JSONRequest having the structure {name: IMG_yyyyMMdd_HHmmss, image: byteArray}
     *
     * @param bitmap, the bitmap that needs to be sent to backend.
     * @param queue, the requests queue containing them
     */
>>>>>>> Stashed changes
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
                if (response.getString("status").equals("200"))
                    Toast.makeText(this@MainActivity, "Image successfully sent.", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@MainActivity, "[IMAGE] Communication error.", Toast.LENGTH_SHORT).show()
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