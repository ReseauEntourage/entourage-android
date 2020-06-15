package social.entourage.android.user.edit

import dagger.Module
import dagger.Provides

/**
 * Module related to UserEditFragment
 * Created by mihaiionescu on 01/11/16.
 */
@Module
class UserEditModule(private val fragment: UserEditFragment) {
    @Provides
    fun providesUserEditFragment(): UserEditFragment {
        return fragment
    }

}