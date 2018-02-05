package com.dega.stories

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_stories.*

/**
 * Created by ddelgado on 04/02/2018.
 */
class StoriesFragment : Fragment(), StoriesContract.View {

    lateinit var presenter: StoriesPesenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_stories, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openGalleryBtn.setOnClickListener({ presenter.openGallery() })

        makeVideoBtn.setOnClickListener({ presenter.requestVideoCapture() })
    }

    override fun setPresenter(presenter: StoriesContract.Presenter) {
        this.presenter = presenter as StoriesPesenter
    }

    override fun setVideoURI(videoUri: Uri) {
        videoView.setVideoURI(videoUri)
        videoView.start()
    }

    override fun explainPermissions(textExplanation: String, requestPermission: () -> Unit) {
        Snackbar.make(root, textExplanation,
                Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.ok), { requestPermission() }).show()
    }

}
