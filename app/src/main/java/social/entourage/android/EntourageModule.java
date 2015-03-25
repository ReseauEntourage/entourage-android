package social.entourage.android;

import android.app.Application;

import social.entourage.android.api.ApiModule;
import social.entourage.android.authentication.AuthenticationModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                EntourageApplication.class
        },
        includes = {
                ApiModule.class,
                AuthenticationModule.class,
        },
        library = true
)
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
