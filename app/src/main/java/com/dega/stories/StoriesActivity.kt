package com.dega.stories

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.app_bar_layout.*


class StoriesActivity : AppCompatActivity() {

    lateinit var presenter: StoriesContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Log.e("MAIN", "HUEVOS!")

        val fragment = StoriesFragment();
        supportFragmentManager.setView { add(R.id.content_frame, fragment) }
        presenter = StoriesPesenter(this, fragment)
        fragment.setPresenter(presenter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Delegate the result from activity launched to the presenter
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Delegate the result from requesting permissions to the presenter
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun FragmentManager.setView(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }
}
