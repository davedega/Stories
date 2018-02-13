package com.dega.stories

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log


/**
 * Created by ddelgado on 04/02/2018.
 */
class StoriesPesenter(private var context: Context,
                      var view: StoriesContract.View)
    : StoriesContract.Presenter {
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_GALLERY_VIDEO -> {
                    data?.let {
                        Log.e("Presenter", "## uri from gallery!... " + it.data)
                        val selectedImageUri = it.data
                        view.setVideoURI(selectedImageUri)
                        view.enableButton()
                    }
                }
                REQUEST_VIDEO_CAPTURE -> {
                    data?.let {
                        Log.e("Presenter", "## uri from video recorded!... " + it.data)
                        view.setVideoURI(it.data)
                        view.enableButton()
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onRequestVideoCapture()
                }
                return
            }
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onOpenGallery()
                }
            }
        }
    }

    override fun onOpenGallery() {

        // check permission to read files

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                view.explainPermissions(context.getString(R.string.gallery_explanation),
                        { requestGalleryPermission() })
            } else {
                requestGalleryPermission()
            }
        } else {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            (context as AppCompatActivity).startActivityForResult(Intent.createChooser(intent,
                    "Select Video"), REQUEST_TAKE_GALLERY_VIDEO)
        }
    }


    override fun onRequestVideoCapture() {

        // check permission to use camera

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                    Manifest.permission.CAMERA)) {
                view.explainPermissions(context.getString(R.string.camera_explanation), { requestCameraPermission() })
            } else {
                requestCameraPermission()
            }
        } else {
            val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            takeVideoIntent.resolveActivity(context.packageManager)?.let {
                (context as AppCompatActivity).startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }


    private fun requestGalleryPermission() {
        ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE)

    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION);
    }

    override fun onUploadVideo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
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

    private fun getImagePath(uri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media.DATA)
            cursor = context.contentResolver.query(uri, proj, null, null, null)
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.let { it.close() }
        }
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 40
        const val REQUEST_TAKE_GALLERY_VIDEO = 30
        const val REQUEST_VIDEO_CAPTURE = 20
        const val REQUEST_READ_EXTERNAL_STORAGE = 10
    }
}
