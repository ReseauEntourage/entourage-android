package social.entourage.android.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import social.entourage.android.BuildConfig;
import social.entourage.android.authentication.AuthenticationInterceptor;
/**
 * Module related to Application
 * Providing API related dependencies
 */
@Module
public class ApiModule {

    @Provides
    @Singleton
    public Endpoint providesEndPoint() {
        return Endpoints.newFixedEndpoint(BuildConfig.ENTOURAGE_URL);
    }

    @Provides
    @Singleton
    public RestAdapter providesRestAdapter(final Endpoint endpoint, final AuthenticationInterceptor interceptor) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .create();

        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(interceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    @Provides
    @Singleton
    public LoginRequest providesLoginService(final RestAdapter restAdapter) {
        return restAdapter.create(LoginRequest.class);
    }

    @Provides
    @Singleton
    public MapRequest providesMapService(final RestAdapter restAdapter) {
        return restAdapter.create(MapRequest.class);
    }

    @Provides
    @Singleton
    public EncounterRequest providesEncounterService(final RestAdapter restAdapter) {
        return restAdapter.create(EncounterRequest.class);
    }

    @Provides
    @Singleton
    public TourRequest providesTourRequest(final RestAdapter restAdapter) {
        return restAdapter.create(TourRequest.class);
    }
}
