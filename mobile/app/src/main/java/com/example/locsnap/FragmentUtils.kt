package com.example.locsnap

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
    }
}