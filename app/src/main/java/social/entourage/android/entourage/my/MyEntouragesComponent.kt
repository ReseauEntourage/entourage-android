package social.entourage.android.entourage.my

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Created by mihaiionescu on 03/08/16.
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [MyEntouragesModule::class])
interface MyEntouragesComponent {
    fun inject(fragment: MyEntouragesFragment?)
    fun getMyEntouragesPresenter(presenter: MyEntouragesPresenter?): MyEntouragesPresenter?
}