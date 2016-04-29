package social.entourage.android;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import social.entourage.android.api.ApiModule;
import social.entourage.android.api.AppRequest;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.MapRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.tape.EncounterTapeService;
import social.entourage.android.api.tape.EncounterTapeTaskQueue;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.AuthenticationModule;
import social.entourage.android.map.tour.TourService;

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
    void inject(TourService service);
    void inject(EncounterTapeService service);

    AuthenticationController getAuthenticationController();

    EncounterTapeTaskQueue getEncounterTapeTaskQueue();

    AppRequest getAppRequest();
    EncounterRequest getEncounterRequest();
    LoginRequest getLoginRequest();
    MapRequest getMapRequest();
    TourRequest getTourRequest();
    UserRequest getUserRequest();
    EntourageRequest getEntourageRequest();
}
