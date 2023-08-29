package com.example.locsnap

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.my_activity_main)

        // Popup per autorizzare l'uso della camera
        if(ContextCompat.checkSelfPermission(this.baseContext, android.Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION),
                1);
        }

        val fragmentTransaction: FragmentTransaction = this.supportFragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack("LocSnap") // It allows to go back to previous screen
        fragmentTransaction.replace(R.id.app_container, LoginFragment()).commit()

        setSupportActionBar(findViewById(R.id.my_toolbar))
    }


    // TODO Separate in multiple activities. Change if condition with isTaskRoot
    override fun onBackPressed() {
        super.onBackPressed()

        // Se si tratta dell'unico fragment sullo stack, esci dall'app.
        if(supportFragmentManager.backStackEntryCount.equals(0)) {
            finish()
        } else {
            val fragment = supportFragmentManager.findFragmentById(R.id.app_container)
            val fragmentTransaction = fragment?.fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.app_container, fragment::class.java.newInstance())?.commit()
        }
    }
}