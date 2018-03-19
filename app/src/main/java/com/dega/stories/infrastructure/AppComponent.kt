package com.dega.stories.infrastructure

import com.dega.stories.Aja
import com.dega.stories.App
import dagger.Component
import javax.inject.Singleton

/**
 * Created by davedega on 15/03/18.
 */
@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {

    fun inject(storiesApp: App)

    fun inject(aja: Aja)

}
