package social.entourage.android.entourage.join

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Created by mihaiionescu on 07/03/16.
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [TourJoinRequestModule::class])
interface TourJoinRequestComponent {
    fun inject(fragment: TourJoinRequestFragment?)
    val tourInformationPresenter: TourJoinRequestPresenter?
}