package com.example.locsnap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.locsnap.utils.FragmentUtils
import com.google.android.material.switchmaterial.SwitchMaterial

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

        // Go to register mode
        switchScreen.setOnCheckedChangeListener { _, isChecked ->
            if(!isChecked) { // User wants to register
                FragmentUtils.transactFragment(this, RegisterFragment())
            }
        }

        loginButton.setOnClickListener {
            UserAuthenticate.userLogin(username.text.toString(), password.text.toString(), this)
        }
    }
}