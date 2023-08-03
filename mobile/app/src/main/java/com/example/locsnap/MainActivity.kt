package com.example.locsnap

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenuto del file layout/activity_main.xml
        setContentView(R.layout.my_activity_main)

        val fragmentTransaction: FragmentTransaction? = this.supportFragmentManager?.beginTransaction()
        fragmentTransaction?.addToBackStack("LocSnap") // It allows to go back to previous screen
        //fragmentTransaction?.addToBackStack(null) // It allows to go back to previous screen
        fragmentTransaction?.replace(R.id.app_container, LoginFragment())?.commit()

        setSupportActionBar(findViewById(R.id.my_toolbar))

        /*val navController = findNavController(R.id.my_content_main)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)*/
    }


    // TODO Separate in multiple activities. Change if condition with isTaskRoot
    override fun onBackPressed() {
        super.onBackPressed()

        // If it's the last fragment and user goes back, then finish
        if(supportFragmentManager.backStackEntryCount.equals(0)) {
            finish()
        } else {
            val fragment = supportFragmentManager.findFragmentById(R.id.app_container)
            val fragmentTransaction = fragment?.fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.app_container, fragment::class.java.newInstance())?.commit()
        }
    }
}