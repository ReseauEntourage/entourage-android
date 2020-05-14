package social.entourage.android.entourage.join

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Created by mihaiionescu on 07/03/16.
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [EntourageJoinRequestModule::class])
interface EntourageJoinRequestComponent {
    fun inject(fragment: EntourageJoinRequestFragment?)
    val entourageInformationPresenter: EntourageJoinRequestPresenter?
}