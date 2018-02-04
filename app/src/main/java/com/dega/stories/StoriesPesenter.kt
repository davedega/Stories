package com.dega.stories

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v7.app.AppCompatActivity
import android.util.Log


/**
 * Created by ddelgado on 04/02/2018.
 */
class StoriesPesenter(private var context: Context,
                      var view: StoriesContract.View)
    : StoriesContract.Presenter {

    override fun openGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        (context as AppCompatActivity).startActivityForResult(Intent.createChooser(intent,
                "Select Video"), REQUEST_TAKE_GALLERY_VIDEO)
    }

    override fun requestVideoCapture() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        takeVideoIntent.resolveActivity(context.packageManager)?.let {
            (context as AppCompatActivity).startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode){
                REQUEST_TAKE_GALLERY_VIDEO ->{
                    data?.let {
                        val selectedImageUri = it.data
                        val filemanagerstring = selectedImageUri.path

                        Log.e("Presenter", "##  selectedImageUri: " + selectedImageUri)


                        filemanagerstring?.let {
                            Log.e("Presenter", "##  filemanager string: " + it)
                        }

                        var selectedImagePath = getPath(selectedImageUri)
                        selectedImagePath?.let {
                            Log.e("Presenter", " selectedImagePath: " + it)
                        }
                    }
                    Log.e("Presenter", "## No image/video selected :( ")
                }
                REQUEST_VIDEO_CAPTURE -> {
                    data?.let {
                        Log.e("Presenter", "## uri from video recorded!... "+it.data)
                        view.setVideoURI(it.data)
                    }
                }
            }
        }
    }

    fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else
            null
    }

    companion object {
        const val REQUEST_TAKE_GALLERY_VIDEO = 20
        const val REQUEST_VIDEO_CAPTURE = 10
    }
}