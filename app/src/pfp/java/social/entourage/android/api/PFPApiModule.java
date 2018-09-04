package social.entourage.android.api;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

/**
 * Provides PFP-related API modules
 * Created by Mihai Ionescu on 06/06/2018.
 */
@Module
public class PFPApiModule {
    @Provides
    @Singleton
    public PrivateCircleRequest providesPrivateCircleRequest(final Retrofit restAdapter) {
        return restAdapter.create(PrivateCircleRequest.class);
    }
}
