package com.example.locsnap

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog.Builder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class FragmentUtils {
    companion object {
        private var friends: MutableList<String> = mutableListOf()

        fun TransactFragment(sourceFragment: Fragment, destinationFragment: Fragment) {
            val fragmentTransaction: FragmentTransaction = sourceFragment.requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.addToBackStack("LocSnap") // It allows to go back to previous screen
            fragmentTransaction.replace(R.id.app_container, destinationFragment)
            fragmentTransaction.commit()
        }

        /**
         * Returns user's friends
         */
        fun getFriends(user: String, fragment: Fragment) {
            Log.d("friends", "getFriends() called.")
            val url = "http://10.0.2.2:8080/get_friends"
            val queue = Volley.newRequestQueue(fragment.requireContext())
            friends.clear()

            val json = JSONObject()
            json.put("username", user)

            val get_friend_request = JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                { response ->
                    if (response.getString("status").equals("200")) {
                        val friendsJson = JSONObject(response.getString("friends"))

                        for (key in friendsJson.keys())
                            friends.add(friendsJson.get(key).toString())

                        val dialog = Builder(fragment.requireContext())
                        dialog.setTitle("Who do you want to send your images to?")
                            .setItems(friends.toTypedArray()
                            ) { _, which ->

                                val intent = Intent(fragment.requireContext(), PickMediaActivity::class.java)
                                intent.putExtra("receiver", friends.get(which))
                                intent.putExtra("shared_by", user) // Actual user is sharing
                                fragment.requireContext().startActivity(intent)
                            }.create().show()
                    }
                }, {}
            )

            queue.add(get_friend_request)
        }
    }
}