package social.entourage.android.newsfeed

import dagger.Component
import social.entourage.android.EntourageComponent
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.ActivityScope

/**
 * Component linked to BaseNewsfeedFragment lifecycle
 * Provide a NewsfeedPresenter
 * @see BaseNewsfeedFragment
 *
 * @see NewsfeedPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [NewsfeedModule::class])
interface BaseNewsfeedComponent {
    fun inject(fragment: BaseNewsfeedFragment?)
    val newsfeedPresenter: NewsfeedPresenter?
    val authenticationController: AuthenticationController
}