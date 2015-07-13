package social.entourage.android.tour;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = TourModule.class
)
public interface TourComponent {
    void inject(TourService service);

    TourServiceManager getTourServiceManager();
}
