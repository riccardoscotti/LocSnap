package com.example.locsnap.activities

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locsnap.R
import com.example.locsnap.utils.PlaceAdapter

class recommendActivity: AppCompatActivity() {
    private var recommendedPlaces: Array<String> = arrayOf()

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras != null) {
            recommendedPlaces = intent.extras!!.getStringArray("recommended_places") as Array<String>
        }

        title = "Discover new places"
        setContentView(R.layout.recommended_places)
        supportActionBar?.hide()

        val recyclerView = findViewById<RecyclerView>(R.id.places_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PlaceAdapter(recommendedPlaces)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(35))
    }

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