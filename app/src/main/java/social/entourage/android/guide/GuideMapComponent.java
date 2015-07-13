package social.entourage.android.guide;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to GuideMapActivity lifecycle
 * Provide a GuideMapPresenter
 * @see GuideMapActivity
 * @see GuideMapPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = GuideMapModule.class
)
@SuppressWarnings("unused")
public interface GuideMapComponent {
    void inject(GuideMapActivity activity);

    GuideMapPresenter getGuideMapPresenter();
}
