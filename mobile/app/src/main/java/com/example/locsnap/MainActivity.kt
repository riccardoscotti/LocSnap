package com.example.locsnap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenuto del file layout/login_scren.xml
        setContentView(R.layout.login_screen)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val username = findViewById<EditText>(R.id.usernameText)
        val password = findViewById<EditText>(R.id.passwordText)
        val queue = Volley.newRequestQueue(this@MainActivity)
        
        val chooseFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val inputStream = uri?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            uploadImage(bitmap, queue)
        }

        loginButton.setOnClickListener {
            userLogin(username.text.toString(), password.text.toString(), queue)
            // TODO Move chooseFile... to another ClickListener, getting it activated by another button.
            //chooseFile.launch("image/*") // Open file chooser
        }


    }

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    private fun userLogin(username: String, password: String, queue: RequestQueue) {
        val jsonObject = JSONObject()
        jsonObject.put("username", username)
        jsonObject.put("password", hashPassword(password))

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
    private fun uploadImage(bitmap: Bitmap?, queue: RequestQueue) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
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
        queue.add(jsonObjectRequest)
    }
}