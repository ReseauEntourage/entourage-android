package social.entourage.android.api

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provides PFP-related API modules
 * Created by Mihai Ionescu on 06/06/2018.
 */
@Module
class PFPApiModule {
    @Provides
    @Singleton
    fun providesPrivateCircleRequest(restAdapter: Retrofit): PrivateCircleRequest {
        return restAdapter.create(PrivateCircleRequest::class.java)
    }
}