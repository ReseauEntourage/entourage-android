package social.entourage.android.entourage.invite

import dagger.Module
import dagger.Provides

/**
 * Created by mihaiionescu on 12/07/16.
 */
@Module
class InviteModule(private val fragment: InviteBaseFragment) {
    @Provides
    fun providesInviteContactsFragment(): InviteBaseFragment {
        return fragment
    }

}