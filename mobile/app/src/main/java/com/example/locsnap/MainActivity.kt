package com.example.locsnap

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenuto del file layout/activity_main.xml
        setContentView(R.layout.my_main)

        val buttonConnect = findViewById<Button>(R.id.connectButton)

        val textView = findViewById<TextView>(R.id.textView)

        buttonConnect.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val url = getString(R.string.base_url) + "/upload"

            val stringRequest = StringRequest(
                Request.Method.GET,
                url,
                { response ->
                    // Handle the response
                    textView.text = response
                },
                { error ->
                    // Handle the error
                    textView.text = error.message
                }
            )

            queue.add(stringRequest)
        }

    }

}