package com.example.locsnap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
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

        // Continue on register mode
        switchScreen.setOnCheckedChangeListener { _, isChecked ->
            if(!isChecked) {
                findNavController().navigate(R.id.action_login_to_register)
            }
        }

        loginButton.setOnClickListener {
            userLogin(username.text.toString(), password.text.toString(), queue)
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
                if (response.getString("status").equals("200")) {

                    val bundle = Bundle()
                    bundle.putString("loggedUsername", username)
                    val fragment = ChooseFragment()
                    fragment.arguments = bundle

                    //val actualFragment = activity?.supportFragmentManager?.findFragmentById(this.id)

                    val fragmentTransaction: FragmentTransaction? = activity?.supportFragmentManager?.beginTransaction()
                    fragmentTransaction?.addToBackStack("LocSnap") // It allows to go back to previous screen
                    fragmentTransaction?.remove(this)?.replace(R.id.app_container, fragment)
                    fragmentTransaction?.commit()

                    // If login's successful, go to chooseMenu
                    //findNavController().navigate(R.id.action_login_to_choose)
                } else if (response.getString("status").equals("401")) {
                    Toast.makeText(this.context, "Login error.", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this.context, "[LOGIN] Communication error.", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(loginRequest)
    }
}