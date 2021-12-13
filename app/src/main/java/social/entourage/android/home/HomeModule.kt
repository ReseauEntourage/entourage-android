package social.entourage.android.home

import dagger.Module
import dagger.Provides

/**
 * Module related to HomeFragment
 * @see HomeFragment
 */
@Module
class HomeModule(private val fragment: HomeFragment) {
    @Provides
    fun providesHomeFragment(): HomeFragment {
        return fragment
    }

}