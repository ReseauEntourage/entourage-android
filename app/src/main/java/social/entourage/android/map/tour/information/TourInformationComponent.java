package social.entourage.android.map.tour.information;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;
import social.entourage.android.map.tour.TourService;

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
