package com.dega.stories

import android.content.Intent
import android.net.Uri

/**
 * Created by ddelgado on 04/02/2018.
 */
interface StoriesContract {

    interface Presenter {

        // Ask for read permissions before open the gallery
        fun openGallery()

        fun requestVideoCapture()

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

        fun requestGalleryPermission()

        fun requestCameraPermission()

    }

    interface View {

        fun setPresenter(presenter: Presenter)

        fun setVideoURI(videoUri: Uri)

        fun explainPermissions(textExplanation: String, requestPermission: () -> Unit)
    }
}