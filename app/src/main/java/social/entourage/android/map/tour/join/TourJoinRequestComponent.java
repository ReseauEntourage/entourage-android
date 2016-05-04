package social.entourage.android.map.tour.join;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;
import social.entourage.android.map.tour.information.TourInformationFragment;
import social.entourage.android.map.tour.information.TourInformationModule;
import social.entourage.android.map.tour.information.TourInformationPresenter;

/**
 * Created by mihaiionescu on 07/03/16.
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = TourJoinRequestModule.class
)
@SuppressWarnings("ununsed")
public interface TourJoinRequestComponent {
    void inject(TourJoinRequestFragment fragment);

    TourJoinRequestPresenter getTourInformationPresenter();
}
