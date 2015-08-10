package social.entourage.android;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import social.entourage.android.api.ApiModule;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.MapRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.AuthenticationModule;
import social.entourage.android.authentication.login.LoginActivity;

/**
 * Dagger component on Application Level
 * Add a get method to provide some object to components that imports this component
 */
@Singleton
@Component(
        modules = {
                EntourageModule.class,
                ApiModule.class,
                AuthenticationModule.class
        }
)
public interface EntourageComponent {
    void inject(Application application);
    void inject(DrawerActivity activity);

    AuthenticationController getAuthenticationController();

    EncounterRequest getEncounterRequest();
    MapRequest getMapRequest();
    LoginRequest getLoginRequest();
    TourRequest getTourRequest();
}
