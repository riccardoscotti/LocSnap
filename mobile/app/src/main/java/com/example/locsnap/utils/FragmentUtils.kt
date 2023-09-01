package com.example.locsnap

import android.content.Intent
import android.util.Log
import android.widget.Toast
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
        fun getFriends(loggedUser: String, fragment: Fragment) {
            val url = "${fragment.resources.getString(R.string.base_url)}/get_friends"
            val queue = Volley.newRequestQueue(fragment.requireContext())
            friends.clear()

            val json = JSONObject()
            json.put("loggedUser", loggedUser)

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
                                intent.putExtra("shared_by", loggedUser) // Actual user is sharing
                                fragment.requireContext().startActivity(intent)
                            }.create().show()
                    }
                }, {}
            )

            queue.add(get_friend_request)
        }

        /*
        * Add new friend to logged user
        * */
        fun addFriend(loggedUser: String, newFriend: String, fragment: Fragment) {
            val url = "${fragment.resources.getString(R.string.base_url)}/add_friend"
            val queue = Volley.newRequestQueue(fragment.requireContext())

            val json = JSONObject()
            json.put("loggedUser", loggedUser)
            json.put("newFriend", newFriend)

            val addFriendRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                { response ->
                    if (response.getString("status").equals("200")) {
                        Toast.makeText(fragment.requireContext(), "${newFriend} è un tuo nuovo amico!", Toast.LENGTH_SHORT).show()
                    } else if (response.getString("status").equals("409")) {
                        Toast.makeText(fragment.requireContext(), "${newFriend} è già presente nella tua lista di amici.", Toast.LENGTH_SHORT).show()
                    } else if(response.getString("status").equals("204")) {
                        Toast.makeText(fragment.requireContext(), "L'utente ${newFriend} non è stato trovato.", Toast.LENGTH_SHORT).show()
                    }
                }, {
                    Toast.makeText(fragment.requireContext(), "Communication error...", Toast.LENGTH_SHORT).show()
                }
            )

            queue.add(addFriendRequest)
        }
    }
}