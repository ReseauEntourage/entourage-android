package social.entourage.android;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class EntourageApplicationModule {

    private final EntourageApplication app;

    public EntourageApplicationModule(final EntourageApplication app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Application providesApplication() {
        return app;
    }
}
