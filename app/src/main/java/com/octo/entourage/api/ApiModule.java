package com.octo.entourage.api;

import com.octo.entourage.BuildConfig;
import com.octo.entourage.authentication.AuthenticationController;
import com.octo.entourage.authentication.AuthenticationInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

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
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(interceptor)
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

}
