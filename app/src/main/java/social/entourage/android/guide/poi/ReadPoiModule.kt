package social.entourage.android.guide.poi

import dagger.Module
import dagger.Provides
import social.entourage.android.api.request.PoiRequest

/**
 * Module related to ReadPoiFragment
 * @see ReadPoiFragment
 */
@Module
internal class ReadPoiModule(private val fragment: ReadPoiFragment,private val request: PoiRequest) {
    @Provides
    fun providesMainPresenter(): ReadPoiPresenter {
        return ReadPoiPresenter(fragment,request)
    }

}