package social.entourage.android.guide;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to GuideMapFragment lifecycle
 * Provide a GuideMapPresenter
 * @see GuideMapFragment
 * @see GuideMapPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = GuideMapModule.class
)
@SuppressWarnings("unused")
public interface GuideMapComponent {
    void inject(GuideMapFragment fragment);

    GuideMapPresenter getGuideMapPresenter();
}
