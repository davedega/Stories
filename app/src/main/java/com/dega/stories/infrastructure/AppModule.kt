package com.dega.stories.infrastructure

import android.content.Context
import com.dega.stories.util.Prefs
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by davedega on 17/03/18.
 */
@Module
class AppModule(val context: Context) {

    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    fun providePrefs() = Prefs(context)
}
