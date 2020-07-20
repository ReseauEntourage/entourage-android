package social.entourage.android

import dagger.Module
import dagger.Provides
import social.entourage.android.user.AvatarUpdatePresenter
import social.entourage.android.user.AvatarUploadView

/**
 * Module related to MainActivity
 */
@Module
class MainModule(private val activity: MainActivity) {
    @Provides
    fun providesActivity(): MainActivity {
        return activity
    }

    @Provides
    fun providesAvatarUploadView(): AvatarUploadView {
        return activity
    }

    @Provides
    fun providesAvatarUpdatePresenter(presenter: MainPresenter): AvatarUpdatePresenter {
        return presenter
    }

}