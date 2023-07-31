package com.example.locsnap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.locsnap.databinding.FragmentFirstBinding
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

        val switchScreen = view.findViewById<SwitchMaterial>(R.id.switchScreen)
        // Continue on login mode
        switchScreen.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                findNavController().navigate(R.id.action_register_to_login)
            }
        }
    }

}