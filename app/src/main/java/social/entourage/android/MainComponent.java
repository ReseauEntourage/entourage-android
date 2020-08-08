package social.entourage.android;

import dagger.Component;
import social.entourage.android.base.ActivityScope;

/**
 * Component linked to MainActivity lifecycle
 * Provides a MainPresenter
 * @see MainActivity
 * @see MainPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = MainModule.class
)
@SuppressWarnings("unused")
public interface MainComponent {
    void inject(MainActivity activity);

    MainPresenter getMainPresenter();
}
