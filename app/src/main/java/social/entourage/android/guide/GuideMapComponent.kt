package social.entourage.android.guide

import dagger.Component
import social.entourage.android.base.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Component linked to GuideMapFragment lifecycle
 * Provide a GuideMapPresenter
 * @see GuideMapFragment
 *
 * @see GuideMapPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [GuideMapModule::class])
interface GuideMapComponent {
    fun inject(fragment: GuideMapFragment?)
    val guideMapPresenter: GuideMapPresenter?
}