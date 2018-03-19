package com.dega.stories;

import android.app.Application;

import com.dega.stories.infrastructure.AppComponent;
import com.dega.stories.infrastructure.AppModule;
import com.dega.stories.infrastructure.DaggerAppComponent;

/**
 * Created by davedega on 17/03/18.
 */

public class App extends Application {

    static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }
    public static AppComponent getComponent() {
        return component;
    }
}
