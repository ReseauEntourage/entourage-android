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
    public PoiRequest providesMapService(final Retrofit restAdapter) {
        return restAdapter.create(PoiRequest.class);
    }
}
