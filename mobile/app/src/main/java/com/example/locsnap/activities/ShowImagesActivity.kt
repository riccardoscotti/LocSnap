package com.example.locsnap.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.locsnap.R
import com.example.locsnap.utils.ImagesAdapter

class ShowImagesActivity : AppCompatActivity() {
    private var bitmapsString: Array<String> = arrayOf()

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : ItemDecoration() {
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
            bitmapsString = intent.extras!!.getStringArray("imagesString") as Array<String>
        }

        var bitmaps = mutableListOf<Bitmap>()

        bitmapsString.forEach {
            val imageBytes = Base64.decode(it, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmaps.add(bitmap)
        }

        title = "Le tue ${bitmaps.size} foto più vicine"
        setContentView(R.layout.show_images)
        supportActionBar?.hide()

        val recyclerView = findViewById<RecyclerView>(R.id.image_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ImagesAdapter(bitmaps)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(35))
    }

    override fun onBackPressed() {
        super.onBackPressed()

        // If it's the last fragment and user goes back, then finish
        if(supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            val fragment = supportFragmentManager.findFragmentById(R.id.app_container)
            val fragmentTransaction = fragment!!.requireFragmentManager().beginTransaction()
            fragmentTransaction.replace(R.id.app_container, fragment::class.java.newInstance()).commit()
        }
    }
}