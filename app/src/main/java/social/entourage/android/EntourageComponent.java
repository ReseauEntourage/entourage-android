package social.entourage.android;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;
import social.entourage.android.api.ApiModule;
import social.entourage.android.api.AppRequest;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.InvitationRequest;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.PFPApiModule;
import social.entourage.android.api.PartnerRequest;
import social.entourage.android.api.PrivateCircleRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.tape.EncounterTapeService;
import social.entourage.android.api.tape.EncounterTapeTaskQueue;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.AuthenticationModule;
import social.entourage.android.authentication.ComplexPreferences;
import social.entourage.android.tour.TourService;

/**
 * Dagger component on Application Level
 * Add a get method to provide some object to components that imports this component
 */
@Singleton
@Component(
        modules = {
                EntourageModule.class,
                ApiModule.class,
                PFPApiModule.class,
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
    TourRequest getTourRequest();
    UserRequest getUserRequest();
    EntourageRequest getEntourageRequest();
    NewsfeedRequest getNewsfeedRequest();
    InvitationRequest getInvitationRequest();
    PartnerRequest getPartnerRequest();

    PrivateCircleRequest getPrivateCircleRequest();

    OkHttpClient getOkHttpClient();

    ComplexPreferences getComplexPreferences();
}
