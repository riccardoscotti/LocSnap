package com.example.locsnap

import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.util.Base64
import android.widget.Toast
import com.example.locsnap.fragments.ChooseFragment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter

class FileManagerUtils {
    companion object {
        private var saved_collections: HashMap<String, Location?> = HashMap()

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

                if (collection.length() > 0)
                    fileWriter.write(",") // Separazione di un file dall'altro, se il file NON è vuoto.

                fileWriter.write(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
                fileWriter.close()
                Toast.makeText(fragment.requireContext(), "Foto aggiunta alla collezione!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(fragment.requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        /**
        * Popups a dialog showing existing collections previously created by user.
        * @param indicates what actions the method will do after user clicks an item on the dialog.
         * "tag" adds the selected friend to the collection's tags.
         * "upload" uploads the collection to the database
        * */
        fun showExistingCollections(fragment: ChooseFragment, action: String, friend: String = "", instantPhoto: Boolean = false) {
            var savedFileNames = mutableListOf<String>()

            if (instantPhoto)
                savedFileNames.add("Capture a photo right now")

            for(key in this.saved_collections.keys) {
                savedFileNames.add(File(key).name)
            }

            val builder = AlertDialog.Builder(fragment.requireContext())
            builder.setTitle("Select the collection you want to upload")
                .setItems(savedFileNames.toTypedArray(),
                    { dialog, which ->
                        if (action.equals("tag")) {
                            if (which.equals(0)) {
                                fragment.openCamera(friend)
                            } else {
                                UploadUtils.tagFriend(fragment.getLoggedUser(), friend,
                                    File(this.saved_collections.keys.elementAt(which-1)).name, fragment)
                            }
                        } else if (action.equals("upload")) {
                            UploadUtils.uploadCollection(
                                File(this.saved_collections.keys.elementAt(which)), fragment)
                        }
                    }
                )
            builder.create().show()
        }
    }
}