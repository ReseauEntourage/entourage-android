package social.entourage.android.entourage.join

import dagger.Module
import dagger.Provides

/**
 * Created by mihaiionescu on 07/03/16.
 */
@Module
class EntourageJoinRequestModule(private val fragment: EntourageJoinRequestFragment) {
    @Provides
    fun providesEntourageJoinRequestFragment(): EntourageJoinRequestFragment {
        return fragment
    }

}