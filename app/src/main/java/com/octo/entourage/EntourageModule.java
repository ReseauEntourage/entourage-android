package com.octo.entourage;

import dagger.Module;

@Module(
        injects = {
                EntourageApplication.class
        }
)
public final class EntourageModule {

    private final EntourageApplication mEntourageApplication;

    public EntourageModule(final EntourageApplication entourageApplication) {
        mEntourageApplication = entourageApplication;
    }
}
