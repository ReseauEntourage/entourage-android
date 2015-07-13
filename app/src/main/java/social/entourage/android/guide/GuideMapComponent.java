package social.entourage.android.guide;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = GuideMapModule.class
)
public interface GuideMapComponent {
    void inject(GuideMapActivity activity);

    GuideMapPresenter getGuideMapPresenter();
}
