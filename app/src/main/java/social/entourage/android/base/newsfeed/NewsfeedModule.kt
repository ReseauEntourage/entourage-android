package social.entourage.android.base.newsfeed

import dagger.Module
import dagger.Provides

/**
 * Module related to NewsfeedFragment
 * @see NewsfeedFragment
 */
@Module
class NewsfeedModule(private val fragment: NewsfeedFragment) {
    @Provides
    fun providesNewsfeedFragment(): NewsfeedFragment {
        return fragment
    }

}