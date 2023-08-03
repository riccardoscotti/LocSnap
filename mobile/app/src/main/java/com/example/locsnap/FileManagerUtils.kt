package com.example.locsnap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.io.*
import android.util.Base64

class FileManagerUtils {
    companion object {
        fun saveNewCollection(fragment: Fragment, uris: List<Uri>) {
            val file = File(fragment
                .requireContext()
                .getExternalFilesDir(null),
                "collection_${fragment.requireContext().getExternalFilesDir(null)?.listFiles()?.size}.bin")

            try {
                val fileWriter = FileWriter(file, true)
                var images = mutableListOf<String>()
                for (uri in uris) {
                    val inputStream = uri.let { fragment.requireActivity().contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    images.add(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
                }
                fileWriter.write(images.joinToString(","))
                fileWriter.close()
            } catch (e: IOException) {
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
                        UploadUtils.uploadCollection(File(filePaths[which]), fragment)
                    })

            builder.create().show()
        }
    }
}