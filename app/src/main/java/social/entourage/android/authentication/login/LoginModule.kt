package social.entourage.android.authentication.login

import dagger.Module
import dagger.Provides
import social.entourage.android.user.AvatarUpdatePresenter
import social.entourage.android.user.AvatarUploadView

/**
 * Module related to LoginActivity
 * @see LoginActivity
 */
@Module
internal class LoginModule(private val activity: LoginActivity) {
    @Provides
    fun providesLoginActivity(): LoginActivity {
        return activity
    }

    @Provides
    fun providesAvatarUploadView(): AvatarUploadView {
        return activity
    }

    @Provides
    fun providesAvatarUpdatePresenter(presenter: LoginPresenter): AvatarUpdatePresenter {
        return presenter
    }

}