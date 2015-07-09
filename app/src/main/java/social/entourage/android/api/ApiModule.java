package social.entourage.android.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.converter.GsonConverter;
import social.entourage.android.BuildConfig;
import social.entourage.android.authentication.AuthenticationInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;

/**
 * Module that provides all the API related dependencies
 */
@Module(
        library = true,
        complete = false
)
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
    public LoginService providesLoginService(final RestAdapter restAdapter) {
        return restAdapter.create(LoginService.class);
    }

    @Provides
    @Singleton
    public MapService providesMapService(final RestAdapter restAdapter) {
        return restAdapter.create(MapService.class);
    }

    @Provides
    @Singleton
    public EncounterService providesEncounterService(final RestAdapter restAdapter) {
        return restAdapter.create(EncounterService.class);
    }

    @Provides
    @Singleton
    public TourRequest providesTourRequest(final RestAdapter restAdapter) {
        return restAdapter.create(TourRequest.class);
    }
}
