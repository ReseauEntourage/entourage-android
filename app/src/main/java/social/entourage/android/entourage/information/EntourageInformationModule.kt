package social.entourage.android.entourage.information

import dagger.Module
import dagger.Provides

/**
 * Module related to EntourageInformationFragment
 * @see EntourageInformationFragment
 */
@Module
class EntourageInformationModule(private val fragment: EntourageInformationFragment) {
    @Provides
    fun providesEntourageInformationFragment(): EntourageInformationFragment {
        return fragment
    }

}