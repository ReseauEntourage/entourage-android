package social.entourage.android.newsfeed

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.newsfeed.NewsfeedModule
import social.entourage.android.newsfeed.NewsfeedPresenter

/**
 * Component linked to BaseNewsfeedFragment lifecycle
 * Provide a NewsfeedPresenter
 * @see BaseNewsfeedFragment
 *
 * @see NewsfeedPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [NewsfeedModule::class])
interface NewsfeedComponent {
    fun inject(fragment: BaseNewsfeedFragment?)
    val newsfeedPresenter: NewsfeedPresenter?
}