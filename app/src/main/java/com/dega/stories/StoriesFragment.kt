package com.dega.stories

import android.content.Intent
import android.os.Bundle
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
        openGalleryBtn.setOnClickListener({presenter.openGallery()})
    }

    override fun setPresenter(presenter: StoriesContract.Presenter) {
        this.presenter = presenter as StoriesPesenter
    }
}