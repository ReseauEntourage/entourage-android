package social.entourage.android.guide.poi

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Component linked to ReadPoiFragment lifecycle
 * Provide a ReadPoiPresenter
 * @see ReadPoiFragment
 *
 * @see ReadPoiPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [ReadPoiModule::class])
interface ReadPoiComponent {
    fun inject(activity: ReadPoiFragment?)
    val readPoiPresenter: ReadPoiPresenter?
}