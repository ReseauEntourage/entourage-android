package social.entourage.android.home

import dagger.Component
import social.entourage.android.EntourageComponent
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.ActivityScope

/**
 * Component linked to HomeFragment lifecycle
 * Provide a HomePresenter
 * @see HomeFragment
 *
 * @see HomePresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [HomeModule::class])
interface HomeComponent {
    fun inject(fragment: HomeFragment?)
    val homePresenter: HomePresenter?
    val authenticationController: AuthenticationController
}