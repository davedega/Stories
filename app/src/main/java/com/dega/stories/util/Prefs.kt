package com.dega.stories.util

import android.content.Context
import android.content.SharedPreferences
import com.dega.stories.R

/**
 * Created by davedega on 15/03/18.
 */
class Prefs(var context: Context) {

    val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), 0);

    var accountName: String
        get() = prefs.getString(context.getString(R.string.pref_account_name), "")
        set(accountName) = prefs.edit().putString(context.getString(R.string.pref_account_name), accountName).apply()

}