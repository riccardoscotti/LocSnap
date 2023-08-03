package com.example.locsnap

import android.os.Bundle
import android.util.Log
import com.example.locsnap.FragmentUtils
import android.widget.Toast
import java.util.concurrent.CountDownLatch
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import java.security.MessageDigest

class UserAuthenticate {

    companion object {

        private var actualStatusCode = ""

        private fun setCode(statusCode: String) {
            Log.d("Auth", "[SET] ActualCode: $statusCode")
            actualStatusCode = statusCode
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

        fun userLogin(username: String, password: String, queue: RequestQueue, sourceFragment: LoginFragment) {

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

            queue.add(loginRequest)
            Log.d("Auth", "[RETURN] actualCode: $actualStatusCode")
        }
    }
}