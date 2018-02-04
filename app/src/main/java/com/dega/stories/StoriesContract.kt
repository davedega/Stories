package com.dega.stories

import android.content.Intent

/**
 * Created by ddelgado on 04/02/2018.
 */
interface StoriesContract {

    interface Presenter {

        fun openGallery()

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    interface View {
        fun setPresenter(presenter: Presenter)
    }
}