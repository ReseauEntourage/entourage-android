package com.octo.entourage.map;

/**
 * Created by RPR on 25/03/15.
 */

import com.octo.entourage.EntourageModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                MapActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public final class MapModule {
    private final MapActivity activity;

    public MapModule(final MapActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public MapPresenter providesMainPresenter() {
        return new MapPresenter(activity);
    }
}
