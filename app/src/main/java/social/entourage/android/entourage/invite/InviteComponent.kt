package social.entourage.android.entourage.invite

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Created by mihaiionescu on 12/07/16.
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [InviteModule::class])
interface InviteComponent {
    fun inject(fragment: InviteBaseFragment?)
    val presenter: InvitePresenter?
}