package social.entourage.android.entourage.join

import dagger.Module
import dagger.Provides

/**
 * Created by mihaiionescu on 07/03/16.
 */
@Module
class TourJoinRequestModule(private val fragment: TourJoinRequestFragment) {
    @Provides
    fun providesTourJoinRequestFragment(): TourJoinRequestFragment {
        return fragment
    }

}