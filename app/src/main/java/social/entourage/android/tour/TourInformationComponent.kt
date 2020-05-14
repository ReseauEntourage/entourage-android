package social.entourage.android.tour

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Component linked to TourInformationFragment lifecycle
 * Provides a TourInformationPresenter
 * @see TourInformationFragment
 *
 * @see TourInformationPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [TourInformationModule::class])
interface TourInformationComponent {
    fun inject(fragment: TourInformationFragment?)
    val tourInformationPresenter: TourInformationPresenter?
}