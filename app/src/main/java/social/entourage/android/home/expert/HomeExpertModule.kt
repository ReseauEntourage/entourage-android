package social.entourage.android.home.expert

import dagger.Module
import dagger.Provides

/**
 * Module related to HomeExpertFragment
 * @see HomeExpertFragment
 */
@Module
class HomeExpertModule(private val fragment: HomeExpertFragment) {
    @Provides
    fun providesHomeExpertFragment(): HomeExpertFragment {
        return fragment
    }

}