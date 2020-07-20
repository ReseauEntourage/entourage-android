package social.entourage.android.user.edit

import dagger.Component
import social.entourage.android.base.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Component linked to UserEditFragment lifecycle
 * Provides an UserEditPresenter
 *
 * @see UserEditFragment
 *
 * @see UserEditPresenter
 *
 * Created by mihaiionescu on 01/11/16.
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [UserEditModule::class])
interface UserEditComponent {
    fun inject(fragment: UserEditFragment?)
    val userPresenter: UserEditPresenter?
}