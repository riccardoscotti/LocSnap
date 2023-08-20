package com.example.locsnap

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import androidx.appcompat.app.AlertDialog.Builder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class FragmentUtils {
    companion object {

        var friends: MutableList<String> = mutableListOf()

        fun TransactFragment(sourceFragment: LoginFragment, destinationFragment: Fragment) {
            val fragmentTransaction: FragmentTransaction? =
                sourceFragment.requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction?.addToBackStack("LocSnap") // It allows to go back to previous screen
            fragmentTransaction?.remove(sourceFragment)?.replace(R.id.app_container, destinationFragment)
            fragmentTransaction?.commit()
        }

        /**
         * Allows to animate a set of elements, given as a hashmap.
         * @param flag Represent if the elements should be drawn up or down.
         * @return Returns a boolean, the opposite of the given one. If an element has gone up, it should come back down.
         */
        fun animateElements(elements: HashMap<View, Float>, flag: Boolean): Boolean {
            for ((key, value) in elements) {
                if (!flag)
                    key.animate().translationY(value).setDuration(400).start()
                else
                    key.animate().translationY(0f).setDuration(400).start()
            }
            return !flag
        }

        /**
         * Returns user's friends
         */
        fun getFriends(user: String, fragment: Fragment) {
            Log.d("friends", "getFriends() called.")
            val url = "http://10.0.2.2:8080/get_friends"
            val queue = Volley.newRequestQueue(fragment.context)
            friends.clear()

            val json = JSONObject()
            json.put("username", user)

            val get_friend_request = JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                { response ->
                    if (response.getString("status").equals("200")) {
                        val friends_json = JSONObject(response.getString("friends"))

                        for (key in friends_json.keys())
                            friends.add(friends_json.get(key).toString())

                        val dialog = Builder(fragment.requireContext())
                        dialog.setTitle("Who do you want to send your images to?")
                            .setItems(friends.toTypedArray(), { dialog, which ->
                                Log.d("friends", "Friend selected: ${friends.get(which)}")

                                shareWith(friends.get(which))
                            }
                        ).create().show()
                    }
                }, {}
            )

            queue.add(get_friend_request)
        }
    }
}