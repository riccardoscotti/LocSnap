package com.example.locsnap

import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.io.*
import android.util.Base64
import android.widget.Toast

class FileManagerUtils {
    companion object {

        private var saved_collections: HashMap<String, Location?> = HashMap()
        private var selected_collection_path: String? = null

        fun getCollections() : HashMap<String, Location?> {
            return this.saved_collections
        }

        fun createNewCollection(capturedPhoto: Bitmap, fragment: Fragment, location: Location? = null) {
            val collection = File(fragment
                .requireContext()
                .getExternalFilesDir(null),
                "collection_${fragment.requireContext().getExternalFilesDir(null)?.listFiles()?.size}.bin")

            this.saved_collections.put(collection.absolutePath, location)
            this.addImageToCollection(collection, capturedPhoto, fragment)
        }

        fun addImageToCollection(collection: File, capturedPhoto: Bitmap, fragment: Fragment) {
            try {
                val fileWriter = FileWriter(collection, true)
                val byteArrayOutputStream = ByteArrayOutputStream()
                capturedPhoto.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                fileWriter.write(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
                fileWriter.close()
                Toast.makeText(fragment.requireContext(), "Foto aggiunta alla collezione!", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(fragment.requireContext(), "Errore...", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        fun showExistingCollections(fragment: Fragment) {

            val builder = AlertDialog.Builder(fragment.requireContext())
            builder.setTitle("Select the collection you want to upload")
                .setItems(this.saved_collections.keys.toTypedArray(),
                    { dialog, which ->
                        repeat(which+1) {
                            this.selected_collection_path = this.saved_collections.keys.iterator().next()
                        }

                        UploadUtils.uploadCollection(File(this.selected_collection_path), fragment)
                    })

            builder.create().show()
        }
    }
}