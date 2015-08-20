package social.entourage.android.map.tour;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to TourInformationFragment lifecycle
 * Provides a TourInformationPresenter
 * @see TourInformationFragment
 * @see TourInformationPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = TourInformationModule.class
)
@SuppressWarnings("ununsed")
public interface TourInformationComponent {
    void inject(TourInformationFragment fragment);

    TourInformationPresenter getTourInformationPresenter();
}
