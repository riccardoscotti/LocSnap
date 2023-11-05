package com.example.locsnap.activities

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.example.locsnap.LoginFragment
import com.example.locsnap.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.my_activity_main)

        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)

        ActivityCompat.requestPermissions(this, permissions, 1);

        val fragmentTransaction: FragmentTransaction = this.supportFragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack("LocSnap") // It allows to go back to previous screen
        fragmentTransaction.replace(R.id.app_container, LoginFragment()).commit()

        setSupportActionBar(findViewById(R.id.my_toolbar))
    }


    // TODO Separate in multiple activities. Change if condition with isTaskRoot
    override fun onBackPressed() {
        super.onBackPressed()

        // Se si tratta dell'unico fragment sullo stack, esci dall'app.
        if(supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            val fragment = supportFragmentManager.findFragmentById(R.id.app_container)
            val fragmentTransaction = fragment!!.requireFragmentManager().beginTransaction()
            fragmentTransaction.replace(R.id.app_container, fragment::class.java.newInstance()).commit()
        }
    }
}