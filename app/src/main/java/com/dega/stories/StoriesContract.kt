package com.dega.stories

import android.content.Intent
import android.net.Uri

/**
 * Created by ddelgado on 04/02/2018.
 */
interface StoriesContract {

    interface Presenter {

        // Dispatch incoming result delegated by the Activity
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        // Dispatch incoming result delegated by the Activity
        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

        fun onOpenGallery()

        fun onRequestVideoCapture()

        fun onUploadVideo()

    }

    interface View {

        fun setPresenter(presenter: Presenter)

        fun setVideoURI(videoUri: Uri)

        fun explainPermissions(textExplanation: String, requestPermission: () -> Unit)

        fun enableButton()
    }
}