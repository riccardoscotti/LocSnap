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
import com.android.volley.toolbox.Volley
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
        val queue = Volley.newRequestQueue(this.context)

        // Continue on register mode
        switchScreen.setOnCheckedChangeListener { _, isChecked ->
            if(!isChecked) {
                findNavController().navigate(R.id.action_login_to_register)
            }
        }

        loginButton.setOnClickListener {
            UserAuthenticate.userLogin(username.text.toString(), password.text.toString(), queue, this)
        }
    }
}