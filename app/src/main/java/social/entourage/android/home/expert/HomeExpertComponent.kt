package social.entourage.android.home.expert

import dagger.Component
import social.entourage.android.EntourageComponent
import social.entourage.android.base.ActivityScope

/**
 * Component linked to HomeExpertFragment lifecycle
 * Provide a HomeExpertPresenter
 * @see HomeExpertFragment
 *
 * @see HomeExpertPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [HomeExpertModule::class])
interface HomeExpertComponent {
    fun inject(fragment: HomeExpertFragment?)
    val homeExpertPresenter: HomeExpertPresenter?
}