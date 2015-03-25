package com.octo.entourage.api;

import com.octo.entourage.BuildConfig;

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
    public RestAdapter providesRestAdapter(final Endpoint endpoint) {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
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
