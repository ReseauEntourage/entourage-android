package social.entourage.android.newsfeed

import dagger.Module
import dagger.Provides

/**
 * Module related to BaseNewsfeedFragment
 * @see BaseNewsfeedFragment
 */
@Module
class NewsfeedModule(private val fragment: BaseNewsfeedFragment) {
    @Provides
    fun providesBaseNewsfeedFragment(): BaseNewsfeedFragment {
        return fragment
    }

}