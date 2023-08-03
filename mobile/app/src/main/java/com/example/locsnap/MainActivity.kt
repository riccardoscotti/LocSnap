package com.example.locsnap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

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