package social.entourage.android.base.newsfeed

import dagger.Component
import social.entourage.android.EntourageComponent
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.ActivityScope

/**
 * Component linked to NewsfeedFragment lifecycle
 * Provide a NewsfeedPresenter
 * @see NewsfeedFragment
 *
 * @see NewsfeedPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [NewsfeedModule::class])
interface NewsfeedComponent {
    fun inject(fragment: NewsfeedFragment?)
    val newsfeedPresenter: NewsfeedPresenter?
    val authenticationController: AuthenticationController
}