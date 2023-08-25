package com.example.locsnap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.io.*
import android.util.Base64
import android.widget.Toast

class FileManagerUtils {
    companion object {
        fun createNewCollection(capturedPhoto: Bitmap, fragment: Fragment) {
            val collection = File(fragment
                .requireContext()
                .getExternalFilesDir(null),
                "collection_${fragment.requireContext().getExternalFilesDir(null)?.listFiles()?.size}.bin")

            this.addToCollection(collection, capturedPhoto, fragment)
        }

        fun addToCollection(collection: File, capturedPhoto: Bitmap, fragment: Fragment) {
            try {
                val fileWriter = FileWriter(collection, true)
                val byteArrayOutputStream = ByteArrayOutputStream()
                capturedPhoto.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                fileWriter.write(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
                fileWriter.close()
                Toast.makeText(fragment.requireContext(), "Foto aggiunta alla collezione!", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(fragment.requireContext(), "Errore...", Toast.LENGTH_SHORT)
                e.printStackTrace()
            }
        }

        fun showExistingCollections(fragment: Fragment) {

            val storedCollections = fragment.requireContext().getExternalFilesDir(null)?.listFiles()
            val fileNames = mutableListOf<String>()
            val filePaths = mutableListOf<String>()
            storedCollections?.forEach { file ->
                fileNames.add(file.name)
                filePaths.add(file.absolutePath)
            }

            val builder = AlertDialog.Builder(fragment.requireContext())
            builder.setTitle("Select the collection you want to upload")
                .setItems(fileNames.toTypedArray(),
                    { dialog, which ->
//                        UploadUtils.uploadCollection(File(filePaths[which]), fragment)
                    })

            builder.create().show()
        }
    }
}