package com.example.locsnap

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction


class FragmentUtils {
    companion object {
        fun TransactFragment(sourceFragment: LoginFragment, destinationFragment: Fragment) {
            val fragmentTransaction: FragmentTransaction? = sourceFragment.requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction?.addToBackStack("LocSnap") // It allows to go back to previous screen
            fragmentTransaction?.remove(sourceFragment)?.replace(R.id.app_container, destinationFragment)
            fragmentTransaction?.commit()
        }

        /**
         * Allows to animate a set of elements, given as a hashmap.
         * @param flag Represent if the elements should be drawn up or down.
         * @return Returns a boolean, the opposite of the given one. If an element has gone up, it should come back down.
         */
        fun animateElements(elements: HashMap<View, Float>, flag: Boolean) : Boolean {
            for ((key, value) in elements) {
                if (!flag)
                    key.animate().translationY(value).setDuration(400).start()
                else
                    key.animate().translationY(0f).setDuration(400).start()
            }
            return !flag
        }

        fun openCamera(activity: Activity) {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            var statusCode = 0
            activity.startActivityForResult(intent, statusCode)
        }
    }
}