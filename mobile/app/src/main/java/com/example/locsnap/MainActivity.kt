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
            val url = "https://www.google.com"

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    textView.text = "Response is: ${response.substring(0, 500)}"
                },
                { textView.text = "That didn't work!" })

            queue.add(stringRequest)
        }

    }

}