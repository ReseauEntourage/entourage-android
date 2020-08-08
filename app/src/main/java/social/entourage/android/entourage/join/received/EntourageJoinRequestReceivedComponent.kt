package social.entourage.android.entourage.join.received

import dagger.Component
import social.entourage.android.base.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Created by mihaiionescu on 18/03/16.
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [EntourageJoinRequestReceivedModule::class])
interface EntourageJoinRequestReceivedComponent {
    fun inject(activity: EntourageJoinRequestReceivedActivity?)
    val joinRequestReceivedPresenter: EntourageJoinRequestReceivedPresenter?
}