package social.entourage.android.entourage.information

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Component linked to EntourageInformationFragment lifecycle
 * Provides a EntourageInformationPresenter
 * @see EntourageInformationFragment
 *
 * @see EntourageInformationPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [EntourageInformationModule::class])
interface EntourageInformationComponent {
    fun inject(fragment: EntourageInformationFragment?)
    val entourageInformationPresenter: EntourageInformationPresenter?
}