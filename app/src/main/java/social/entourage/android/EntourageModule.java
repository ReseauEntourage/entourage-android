package social.entourage.android;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class EntourageModule {

    private final EntourageApplication app;

    public EntourageModule(final EntourageApplication app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Application providesApplication() {
        return app;
    }
}
