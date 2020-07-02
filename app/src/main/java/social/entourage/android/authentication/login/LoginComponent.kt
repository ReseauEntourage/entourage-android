package social.entourage.android.authentication.login

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Component linked to LoginActivity lifecycle
 * Provide a BaseLoginPresenter
 * @see LoginActivity
 *
 * @see BaseLoginPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [LoginModule::class])
interface LoginComponent {
    fun inject(activity: LoginActivity?)
    val loginPresenter: LoginPresenter?
}