package social.entourage.android.entourage.create

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Created by mihaiionescu on 28/04/16.
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [CreateEntourageModule::class])
interface CreateEntourageComponent {
    fun inject(fragment: CreateEntourageFragment?)
    fun getCreateEntouragePresenter(presenter: CreateEntouragePresenter?): CreateEntouragePresenter?
}