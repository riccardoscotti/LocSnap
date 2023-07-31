package com.example.locsnap

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONObject
import java.security.MessageDigest

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val switchScreen = view.findViewById<SwitchMaterial>(R.id.switchScreen) // Allows to switch between register/login mode.
        val username = view.findViewById<EditText>(R.id.usernameText)
        val password = view.findViewById<EditText>(R.id.passwordText)
        val queue = Volley.newRequestQueue(this.context)

        val chooseFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val inputStream = uri?.let { activity?.contentResolver?.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            //uploadImage(bitmap, queue)
        }

        // Continue on register mode
        switchScreen.setOnCheckedChangeListener { _, isChecked ->
            if(!isChecked) {
                findNavController().navigate(R.id.action_login_to_register)
            }
        }

        loginButton.setOnClickListener {
            userLogin(username.text.toString(), password.text.toString(), queue)
            // TODO Move chooseFile... to another ClickListener, getting it activated by another button.
            //chooseFile.launch("image/*") // Open file chooser
        }
    }

    /**
     * Simple password hashing using SHA-256 algorithm.
     * @param password: User password as-is, not hashed.
     * @return password's SHA-256.
     */
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

        val loginRequest = JsonObjectRequest(
            Request.Method.POST,
            getString(R.string.base_url) + "/login",
            jsonObject,
            { response ->
                if (response.getString("status").equals("200"))
                    Toast.makeText(this.context, "Login successful", Toast.LENGTH_SHORT).show()
                else if (response.getString("status").equals("401"))
                    Toast.makeText(this.context, "Login error.", Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(this.context, "[LOGIN] Communication error.", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(loginRequest)
    }

}