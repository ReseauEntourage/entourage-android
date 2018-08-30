package social.entourage.android.api;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

/**
 * Module related to Application
 * Providing Entourage specific API related dependencies
 */
@Module
public class EntourageApiModule {
    @Provides
    @Singleton
    public MapRequest providesMapService(final Retrofit restAdapter) {
        return restAdapter.create(MapRequest.class);
    }
}
