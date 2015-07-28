package social.entourage.android.map.tour;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

/**
 * Component linked to TourService lifecycle
 * Provide a TourServiceManager
 * @see TourService
 * @see TourServiceManager
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = TourModule.class
)
@SuppressWarnings("unused")
public interface TourComponent {
    void inject(TourService service);

    TourServiceManager getTourServiceManager();
}
