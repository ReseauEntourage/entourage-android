package social.entourage.android.user

import dagger.Module
import dagger.Provides

/**
 * Module related to UserFragment
 * @see UserFragment
 */
@Module
class UserModule(private val fragment: UserFragment) {
    @Provides
    fun providesUserFragment(): UserFragment {
        return fragment
    }

}