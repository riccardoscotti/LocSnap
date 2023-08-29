package com.example.locsnap

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.security.MessageDigest

class UserAuthenticate {
    companion object {

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

        fun userLogin(username: String, password: String, sourceFragment: Fragment) {

            val jsonObject = JSONObject()
            jsonObject.put("username", username)
            jsonObject.put("password", hashPassword(password))

            val loginRequest = JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2:8080/login",
                jsonObject,
                { response ->
                if (response.getString("status").equals("200")) {

                    val bundle = Bundle()
                    bundle.putString("loggedUsername", username)
                    val chooseFragment = ChooseFragment()
                    chooseFragment.arguments = bundle

                    FragmentUtils.TransactFragment(sourceFragment, chooseFragment)

                } else if (response.getString("status").equals("401")) {
                    Toast.makeText(sourceFragment.requireActivity().baseContext, "Login error.", Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(sourceFragment.requireActivity().baseContext, "[LOGIN] Communication error.", Toast.LENGTH_SHORT).show()
            })

            val queue = Volley.newRequestQueue(sourceFragment.context)
            queue.add(loginRequest)
        }

        fun registerUser(name: String, surname: String, username: String, password: String, sourceFragment: Fragment) {
            val jsonObject = JSONObject()

            jsonObject.put("name", name)
            jsonObject.put("surname", surname)
            jsonObject.put("username", username)
            jsonObject.put("password", hashPassword(password))

            val loginRequest = JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2:8080/signup",
                jsonObject,
                { response ->
                    if (response.getString("status").equals("200")) {
                        val bundle = Bundle()
                        bundle.putString("loggedUsername", username)
                        val chooseFragment = ChooseFragment()
                        chooseFragment.arguments = bundle
                        FragmentUtils.TransactFragment(sourceFragment, chooseFragment)

                    } else if (response.getString("status").equals("401")) {
                        Toast.makeText(sourceFragment.requireActivity().baseContext, "Login error.", Toast.LENGTH_SHORT).show()
                    }
                }, {
                    Toast.makeText(sourceFragment.requireActivity().baseContext, "[LOGIN] Communication error.", Toast.LENGTH_SHORT).show()
                })

            val queue = Volley.newRequestQueue(sourceFragment.context)
            queue.add(loginRequest)
        }
    }
}