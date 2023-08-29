package com.example.locsnap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameText = view.findViewById<TextView>(R.id.nameText)
        val surnameText = view.findViewById<TextView>(R.id.surnameText)
        val usernameText = view.findViewById<TextView>(R.id.usernameText)
        val passwordText = view.findViewById<TextView>(R.id.passwordText)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val switchScreen = view.findViewById<SwitchMaterial>(R.id.switchScreen) // Allows to switch between register/login mode.

        loginButton.setOnClickListener {
            UserAuthenticate.registerUser(nameText.text.toString(), surnameText.text.toString(),
                usernameText.text.toString(), passwordText.text.toString(), this)
        }

        // Continue on register mode
        switchScreen.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { // User wants to register
                FragmentUtils.TransactFragment(this, LoginFragment())
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }
}