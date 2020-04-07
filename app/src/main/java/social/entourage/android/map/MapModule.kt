package social.entourage.android.map

import dagger.Module
import dagger.Provides

/**
 * Module related to MapFragment
 * @see MapFragment
 */
@Module
class MapModule(private val fragment: MapFragment) {
    @Provides
    fun providesMapFragment(): MapFragment {
        return fragment
    }

}