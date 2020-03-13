package social.entourage.android.guide.poi

import dagger.Module
import dagger.Provides

/**
 * Module related to ReadPoiFragment
 * @see ReadPoiFragment
 */
@Module
internal class ReadPoiModule(private val fragment: ReadPoiFragment) {
    @Provides
    fun providesMainPresenter(): ReadPoiPresenter {
        return ReadPoiPresenter(fragment)
    }

}