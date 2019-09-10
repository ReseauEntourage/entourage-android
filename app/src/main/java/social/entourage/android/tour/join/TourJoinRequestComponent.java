package social.entourage.android.tour.join;

import dagger.Component;
import social.entourage.android.ActivityScope;
import social.entourage.android.EntourageComponent;

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

    TourJoinRequestPresenter getEntourageInformationPresenter();
}
