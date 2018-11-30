package social.entourage.android.authentication.login;

import dagger.Module;
import dagger.Provides;
import social.entourage.android.authentification.login.LoginPresenter;
import social.entourage.android.user.AvatarUpdatePresenter;
import social.entourage.android.user.AvatarUploadView;

/**
 * Module related to LoginActivity
 * @see LoginActivity
 */
@Module
class LoginModule {

    private final LoginActivity activity;

    public LoginModule(final LoginActivity activity) {
        this.activity = activity;
    }

    @Provides
    public LoginActivity providesLoginActivity() {
        return activity;
    }

    @Provides
    public AvatarUploadView providesAvatarUploadView() { return activity; }

    @Provides
    public AvatarUpdatePresenter providesAvatarUpdatePresenter(LoginPresenter presenter) { return presenter; }

}
